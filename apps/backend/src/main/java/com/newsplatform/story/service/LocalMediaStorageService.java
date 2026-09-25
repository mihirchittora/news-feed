package com.newsplatform.story.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.story.entity.StoryMediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalMediaStorageService implements MediaStorageService {
    private final Path storagePath; private final String publicUrl; private final long maxImageSize; private final long maxVideoSize;
    public LocalMediaStorageService(@Value("${app.media.storage-path:./data/media}") String storagePath, @Value("${app.media.public-url:http://localhost:8080}") String publicUrl,
                                    @Value("${app.media.max-image-upload-size:10485760}") long maxImageSize, @Value("${app.media.max-video-upload-size:52428800}") long maxVideoSize) {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize(); this.publicUrl = publicUrl.replaceAll("/$", ""); this.maxImageSize = maxImageSize; this.maxVideoSize = maxVideoSize;
    }
    @Override public StoredMedia store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw bad("EMPTY_MEDIA", "Choose a file to upload");
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        StoryMediaType type = imageType(mime) ? StoryMediaType.IMAGE : videoType(mime) ? StoryMediaType.VIDEO : null;
        if (type == null) throw bad("UNSUPPORTED_MEDIA_TYPE", "Only JPEG, PNG, WebP images and MP4 video are supported");
        long max = type == StoryMediaType.IMAGE ? maxImageSize : maxVideoSize;
        if (file.getSize() > max) throw bad("MEDIA_TOO_LARGE", "The uploaded file exceeds the configured size limit");
        try {
            byte[] header = readHeader(file);
            if (!matchesContent(type, mime, header)) throw bad("MEDIA_CONTENT_MISMATCH", "The file content does not match its declared media type");
            String extension = type == StoryMediaType.VIDEO ? ".mp4" : mime.equals("image/png") ? ".png" : mime.equals("image/webp") ? ".webp" : ".jpg";
            String key = UUID.randomUUID() + extension; Files.createDirectories(storagePath); Path destination = storagePath.resolve(key).normalize();
            if (!destination.getParent().equals(storagePath)) throw bad("INVALID_MEDIA_KEY", "Invalid media path");
            Path temporary = Files.createTempFile(storagePath, ".upload-", ".tmp");
            try {
                try (InputStream input = file.getInputStream()) { Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING); }
                if (file.getSize() >= 0 && Files.size(temporary) != file.getSize()) {
                    throw bad("MEDIA_UPLOAD_INCOMPLETE", "The uploaded file could not be read completely");
                }
                moveIntoPlace(temporary, destination);
            } finally {
                Files.deleteIfExists(temporary);
            }
            return new StoredMedia(key, mime, file.getSize(), publicUrl + "/api/v1/media/" + key);
        } catch (IOException ex) { throw new RbacException(HttpStatus.INTERNAL_SERVER_ERROR, "MEDIA_STORAGE_ERROR", "The media could not be stored"); }
    }
    @Override public void delete(String storageKey) { if (storageKey == null || storageKey.contains("/") || storageKey.contains("\\")) return; try { Files.deleteIfExists(storagePath.resolve(storageKey).normalize()); } catch (IOException ignored) { } }
    private byte[] readHeader(MultipartFile file) throws IOException { try (InputStream input = file.getInputStream()) { return input.readNBytes(16); } }
    private boolean imageType(String mime) { return mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/webp"); }
    private boolean videoType(String mime) { return mime.equals("video/mp4"); }
    private boolean matchesContent(StoryMediaType type, String mime, byte[] header) {
        if (type == StoryMediaType.IMAGE && mime.equals("image/jpeg")) return header.length > 2 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8;
        if (type == StoryMediaType.IMAGE && mime.equals("image/png")) return header.length > 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
        if (type == StoryMediaType.IMAGE && mime.equals("image/webp")) return header.length > 11 && ascii(header, 0, 4, "RIFF") && ascii(header, 8, 4, "WEBP");
        return type == StoryMediaType.VIDEO && header.length > 12 && ascii(header, 4, 4, "ftyp");
    }
    private boolean ascii(byte[] bytes, int start, int length, String value) { if (bytes.length < start + length) return false; for (int i = 0; i < length; i++) if (bytes[start + i] != value.charAt(i)) return false; return true; }
    private void moveIntoPlace(Path source, Path destination) throws IOException {
        try { Files.move(source, destination, java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
        catch (java.nio.file.AtomicMoveNotSupportedException ignored) { Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING); }
    }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
}
