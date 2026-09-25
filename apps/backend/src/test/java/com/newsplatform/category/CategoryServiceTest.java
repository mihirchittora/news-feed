package com.newsplatform.category;

import com.newsplatform.category.dto.CategoryRequest;
import com.newsplatform.category.entity.Category;
import com.newsplatform.category.entity.CategoryStatus;
import com.newsplatform.category.repository.CategoryRepository;
import com.newsplatform.category.service.CategoryService;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock private CategoryRepository categoryRepository;
    @Mock private StoryRepository storyRepository;
    @Mock private AuditService auditService;

    @Test
    void createsChildUnderTopLevelCategory() {
        UUID parentId = UUID.randomUUID();
        Category parent = category("Sports", "sports", null);
        ReflectionTestUtils.setField(parent, "id", parentId);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.existsBySlug("cricket")).thenReturn(false);
        when(categoryRepository.existsSiblingName("Cricket", parentId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        CategoryService service = new CategoryService(categoryRepository, storyRepository, auditService);
        var response = service.create(new CategoryRequest("Cricket", null, "Cricket news", "ACTIVE", 1, parentId), UUID.randomUUID());

        assertThat(response.parentId()).isEqualTo(parentId);
        assertThat(response.name()).isEqualTo("Cricket");
    }

    @Test
    void rejectsChildOfChildAndDeletingCategoryWithChildren() {
        UUID grandparentId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        Category grandparent = category("Sports", "sports", null);
        Category parent = category("Cricket", "cricket", grandparent);
        ReflectionTestUtils.setField(grandparent, "id", grandparentId);
        ReflectionTestUtils.setField(parent, "id", parentId);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CategoryService service = new CategoryService(categoryRepository, storyRepository, auditService);
        assertThatThrownBy(() -> service.create(new CategoryRequest("IPL", null, null, "ACTIVE", 0, parentId), UUID.randomUUID()))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("CATEGORY_DEPTH_EXCEEDED"));

        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.existsByParentId(parentId)).thenReturn(true);
        assertThatThrownBy(() -> service.delete(parentId, UUID.randomUUID()))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("CATEGORY_HAS_CHILDREN"));
        verify(storyRepository, never()).existsByCategoryId(parentId);
    }

    @Test
    void rejectsAssigningDuplicateSiblingName() {
        when(categoryRepository.existsBySlug("sports")).thenReturn(false);
        when(categoryRepository.existsSiblingName("Sports", null)).thenReturn(true);
        CategoryService service = new CategoryService(categoryRepository, storyRepository, auditService);

        assertThatThrownBy(() -> service.create(new CategoryRequest(" Sports ", null, null, null, 0, null), UUID.randomUUID()))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("DUPLICATE_CATEGORY_NAME"));
    }

    private Category category(String name, String slug, Category parent) {
        return new Category(name, slug, null, CategoryStatus.ACTIVE, 0, parent);
    }
}
