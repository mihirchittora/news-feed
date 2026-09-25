package com.newsplatform.story;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.story.service.LocalMediaStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalMediaStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesValidJpegWithAnInternalKey() {
        LocalMediaStorageService service = new LocalMediaStorageService(tempDir.toString(), "http://localhost:8080", 1024, 2048);
        byte[] jpeg = {(byte) 0xff, (byte) 0xd8, 0x01, 0x02, 0x03};
        var stored = service.store(new MockMultipartFile("file", "../../unsafe.jpg", "image/jpeg", jpeg));
        assertThat(stored.storageKey()).doesNotContain("unsafe", "..", "/");
        assertThat(stored.publicUrl()).contains("/api/v1/media/");
        service.delete(stored.storageKey());
    }

    @Test
    void rejectsUnsupportedContent() {
        LocalMediaStorageService service = new LocalMediaStorageService(tempDir.toString(), "http://localhost:8080", 1024, 2048);
        assertThatThrownBy(() -> service.store(new MockMultipartFile("file", "story.txt", "text/plain", "not media".getBytes())))
                .isInstanceOf(RbacException.class)
                .hasMessageContaining("supported");
    }
}
