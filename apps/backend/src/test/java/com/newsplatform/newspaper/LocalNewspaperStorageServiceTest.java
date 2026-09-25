package com.newsplatform.newspaper;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.newspaper.service.LocalNewspaperStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalNewspaperStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesPdfInNewspaperNamespaceWithoutTrustingFilename() {
        LocalNewspaperStorageService service = new LocalNewspaperStorageService(tempDir.toString(), 1024, 1024);
        var stored = service.storePdf(new MockMultipartFile("file", "../../unsafe.pdf", "text/plain", "%PDF-1.7\nnews".getBytes()), LocalDate.of(2026, 9, 25));

        assertThat(stored.storageKey()).startsWith("newspapers/2026/09/25/").endsWith(".pdf");
        assertThat(service.resolve(stored.storageKey())).isRegularFile();
        service.delete(stored.storageKey());
        assertThatThrownBy(() -> service.resolve(stored.storageKey())).isInstanceOf(RbacException.class);
    }

    @Test
    void rejectsPdfWithInvalidSignature() {
        LocalNewspaperStorageService service = new LocalNewspaperStorageService(tempDir.toString(), 1024, 1024);
        assertThatThrownBy(() -> service.storePdf(new MockMultipartFile("file", "paper.pdf", "application/pdf", "not a pdf".getBytes()), LocalDate.now()))
                .isInstanceOfSatisfying(RbacException.class, exception -> assertThat(exception.getCode()).isEqualTo("INVALID_NEWSPAPER_PDF"));
    }
}
