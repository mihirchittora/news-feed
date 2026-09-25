package com.newsplatform.newspaper.service;

import com.newsplatform.common.error.RbacException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalNewspaperStorageService implements NewspaperStorageService {
    private final Path storagePath;
    private final long maxPdfSize;
    private final long maxCoverSize;

    public LocalNewspaperStorageService(@Value("${app.media.storage-path:./data/media}") String storagePath,
                                         @Value("${app.newspaper.max-pdf-upload-size:104857600}") long maxPdfSize,
                                         @Value("${app.media.max-image-upload-size:10485760}") long maxCoverSize) {
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.maxPdfSize = maxPdfSize;
        this.maxCoverSize = maxCoverSize;
    }

    @Override
    public StoredFile storePdf(MultipartFile file, LocalDate editionDate) {
        if (file == null || file.isEmpty()) throw bad("EMPTY_NEWSPAPER_DOCUMENT", "Choose a newspaper PDF to upload");
        if (file.getSize() > maxPdfSize) throw bad("NEWSPAPER_DOCUMENT_TOO_LARGE", "The newspaper PDF exceeds the configured size limit");
        try {
            byte[] header = readHeader(file, 8);
            if (header.length < 5 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F' || header[4] != '-') {
                throw bad("INVALID_NEWSPAPER_PDF", "The uploaded document is not a valid PDF");
            }
            return store(file, editionDate, ".pdf", "application/pdf");
        } catch (IOException exception) {
            throw new RbacException(HttpStatus.INTERNAL_SERVER_ERROR, "NEWSPAPER_STORAGE_ERROR", "The newspaper document could not be stored");
        }
    }

    @Override
    public StoredFile storeCover(MultipartFile file, LocalDate editionDate) {
        if (file == null || file.isEmpty()) throw bad("EMPTY_NEWSPAPER_COVER", "Choose a cover image to upload");
        if (file.getSize() > maxCoverSize) throw bad("NEWSPAPER_COVER_TOO_LARGE", "The cover image exceeds the configured size limit");
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw bad("INVALID_NEWSPAPER_COVER", "Only JPEG, PNG, or WebP cover images are supported");
        };
        try {
            if (!matchesImage(mime, readHeader(file, 16))) throw bad("INVALID_NEWSPAPER_COVER", "The cover image content does not match its declared type");
            return store(file, editionDate, extension, mime);
        } catch (IOException exception) {
            throw new RbacException(HttpStatus.INTERNAL_SERVER_ERROR, "NEWSPAPER_STORAGE_ERROR", "The cover image could not be stored");
        }
    }

    @Override
    public Path resolve(String storageKey) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("\\") || storageKey.contains("..")) {
            throw notFound();
        }
        Path path = storagePath.resolve(storageKey).normalize();
        if (!path.startsWith(storagePath) || !Files.isRegularFile(path)) throw notFound();
        return path;
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("\\") || storageKey.contains("..")) return;
        try {
            Path path = storagePath.resolve(storageKey).normalize();
            if (path.startsWith(storagePath)) Files.deleteIfExists(path);
        } catch (IOException ignored) { }
    }

    private StoredFile store(MultipartFile file, LocalDate editionDate, String extension, String mime) throws IOException {
        Path directory = storagePath.resolve("newspapers").resolve(String.valueOf(editionDate.getYear()))
                .resolve(String.format("%02d", editionDate.getMonthValue())).resolve(String.format("%02d", editionDate.getDayOfMonth())).normalize();
        if (!directory.startsWith(storagePath)) throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_NEWSPAPER_KEY", "Invalid newspaper storage path");
        Files.createDirectories(directory);
        String key = storagePath.relativize(directory.resolve(UUID.randomUUID() + extension)).toString().replace(java.io.File.separatorChar, '/');
        Path destination = storagePath.resolve(key).normalize();
        Path temporary = Files.createTempFile(directory, ".upload-", ".tmp");
        try {
            try (InputStream input = file.getInputStream()) { Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING); }
            if (file.getSize() >= 0 && Files.size(temporary) != file.getSize()) {
                throw bad("UPLOAD_INCOMPLETE", "The uploaded file could not be read completely");
            }
            try { Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ignored) { Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING); }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return new StoredFile(key, mime, file.getSize());
    }

    private byte[] readHeader(MultipartFile file, int length) throws IOException { try (InputStream input = file.getInputStream()) { return input.readNBytes(length); } }
    private boolean matchesImage(String mime, byte[] header) {
        if (mime.equals("image/jpeg")) return header.length > 2 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8;
        if (mime.equals("image/png")) return header.length > 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
        return header.length > 11 && ascii(header, 0, 4, "RIFF") && ascii(header, 8, 4, "WEBP");
    }
    private boolean ascii(byte[] bytes, int start, int length, String value) { if (bytes.length < start + length) return false; for (int i = 0; i < length; i++) if (bytes[start + i] != value.charAt(i)) return false; return true; }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
    private RbacException notFound() { return new RbacException(HttpStatus.NOT_FOUND, "NEWSPAPER_FILE_NOT_FOUND", "Newspaper file not found"); }
}
