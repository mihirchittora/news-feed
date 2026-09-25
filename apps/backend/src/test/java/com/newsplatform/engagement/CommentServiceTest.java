package com.newsplatform.engagement;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.engagement.dto.CreateCommentRequest;
import com.newsplatform.engagement.dto.UpdateCommentRequest;
import com.newsplatform.engagement.entity.Comment;
import com.newsplatform.engagement.repository.CommentRepository;
import com.newsplatform.engagement.service.CommentService;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.repository.StoryRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CommentServiceTest {
    @Mock private CommentRepository commentRepository;
    @Mock private StoryRepository storyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    private CommentService commentService;
    private Story publishedStory;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, storyRepository, userRepository, auditService, 20);
        publishedStory = new Story("Published story", "published-story", "Summary", "Body", null,
                new User("Author", "author@example.com", "hash", UserStatus.ACTIVE));
        publishedStory.publish();
    }

    @Test
    void trimsAndPersistsAComment() {
        UUID storyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("Reader", "reader@example.com", "hash", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(publishedStory));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = commentService.create(storyId, userId, new CreateCommentRequest("  Useful update.  "));

        assertThat(response.body()).isEqualTo("Useful update.");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void rejectsWhitespaceOnlyAndOversizedComments() {
        UUID storyId = UUID.randomUUID();
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(publishedStory));

        assertThatThrownBy(() -> commentService.create(storyId, UUID.randomUUID(), new CreateCommentRequest("   ")))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("COMMENT_EMPTY"));
        assertThatThrownBy(() -> commentService.create(storyId, UUID.randomUUID(), new CreateCommentRequest("123456789012345678901")))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("COMMENT_TOO_LONG"));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void onlyTheCommentOwnerCanDelete() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User owner = new User("Reader", "reader@example.com", "hash", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(owner, "id", ownerId);
        Comment comment = new Comment(publishedStory, owner, "My comment");
        when(commentRepository.findWithModerationDetailsById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteOwn(commentId, otherUserId))
                .isInstanceOfSatisfying(RbacException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("COMMENT_NOT_OWNER");
                });
        assertThat(comment.getStatus().name()).isEqualTo("VISIBLE");
    }

    @Test
    void onlyTheCommentOwnerCanUpdate() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        User owner = new User("Reader", "reader@example.com", "hash", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(owner, "id", ownerId);
        Comment comment = new Comment(publishedStory, owner, "Original comment");
        when(commentRepository.findWithModerationDetailsById(commentId)).thenReturn(Optional.of(comment));

        var response = commentService.updateOwn(commentId, ownerId, new UpdateCommentRequest("  Updated comment  "));

        assertThat(response.body()).isEqualTo("Updated comment");
        assertThat(comment.getBody()).isEqualTo("Updated comment");
    }
}
