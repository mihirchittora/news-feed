package com.newsplatform.newspaper.service;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.newsplatform.common.error.RbacException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/** Private Object Storage-backed newspaper files. The API performs authorization before opening an object. */
@Service
@ConditionalOnProperty(name = "app.media.storage-type", havingValue = "oci")
public class OciNewspaperStorageService implements NewspaperStorageService {
    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucket;
    private final long maxPdfSize;
    private final long maxCoverSize;

    public OciNewspaperStorageService(ObjectStorageClient client,
                                      @Value("${app.media.oci.namespace}") String namespace,
                                      @Value("${app.media.oci.bucket}") String bucket,
                                      @Value("${app.newspaper.max-pdf-upload-size:104857600}") long maxPdfSize,
                                      @Value("${app.media.max-image-upload-size:10485760}") long maxCoverSize) {
        this.client = client;
        this.namespace = required(namespace, "OCI_OBJECT_STORAGE_NAMESPACE");
        this.bucket = required(bucket, "OCI_OBJECT_STORAGE_BUCKET");
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
        } catch (IOException exception) { throw storageError("The newspaper document could not be stored"); }
    }

    @Override
    public StoredFile storeCover(MultipartFile file, LocalDate editionDate) {
        if (file == null || file.isEmpty()) throw bad("EMPTY_NEWSPAPER_COVER", "Choose a cover image to upload");
        if (file.getSize() > maxCoverSize) throw bad("NEWSPAPER_COVER_TOO_LARGE", "The cover image exceeds the configured size limit");
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = switch (mime) { case "image/jpeg" -> ".jpg"; case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> throw bad("INVALID_NEWSPAPER_COVER", "Only JPEG, PNG, or WebP cover images are supported"); };
        try {
            byte[] header = readHeader(file, 16);
            if (!matchesImage(mime, header)) throw bad("INVALID_NEWSPAPER_COVER", "The cover image content does not match its declared type");
            return store(file, editionDate, extension, mime);
        } catch (IOException exception) { throw storageError("The cover image could not be stored"); }
    }

    @Override
    public Path resolve(String storageKey) { throw new UnsupportedOperationException("OCI newspaper files are streamed from Object Storage"); }

    @Override
    public Resource open(String storageKey) {
        if (!safeKey(storageKey)) throw notFound();
        try {
            return new InputStreamResource(client.getObject(GetObjectRequest.builder().namespaceName(namespace).bucketName(bucket).objectName(storageKey).build()).getInputStream());
        } catch (BmcException exception) { throw notFound(); }
    }

    @Override
    public void delete(String storageKey) {
        if (!safeKey(storageKey)) return;
        try { client.deleteObject(DeleteObjectRequest.builder().namespaceName(namespace).bucketName(bucket).objectName(storageKey).build()); }
        catch (BmcException ignored) { }
    }

    private StoredFile store(MultipartFile file, LocalDate date, String extension, String mime) throws IOException {
        String key = String.format("newspapers/%04d/%02d/%02d/%s%s", date.getYear(), date.getMonthValue(), date.getDayOfMonth(), UUID.randomUUID(), extension);
        try (InputStream input = file.getInputStream()) {
            client.putObject(PutObjectRequest.builder().namespaceName(namespace).bucketName(bucket).objectName(key)
                    .contentType(mime).contentLength(file.getSize()).putObjectBody(input).build());
        }
        return new StoredFile(key, mime, file.getSize());
    }
    private byte[] readHeader(MultipartFile file, int length) throws IOException { try (InputStream input = file.getInputStream()) { return input.readNBytes(length); } }
    private boolean matchesImage(String mime, byte[] header) { if (mime.equals("image/jpeg")) return header.length > 2 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8; if (mime.equals("image/png")) return header.length > 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47; return header.length > 11 && ascii(header, 0, 4, "RIFF") && ascii(header, 8, 4, "WEBP"); }
    private boolean ascii(byte[] bytes, int start, int length, String value) { if (bytes.length < start + length) return false; for (int i = 0; i < length; i++) if (bytes[start + i] != value.charAt(i)) return false; return true; }
    private boolean safeKey(String key) { return key != null && key.startsWith("newspapers/") && !key.contains("\\") && !key.contains("..") && !key.isBlank(); }
    private String required(String value, String name) { if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required when MEDIA_STORAGE_TYPE=oci"); return value; }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
    private RbacException notFound() { return new RbacException(HttpStatus.NOT_FOUND, "NEWSPAPER_FILE_NOT_FOUND", "Newspaper file not found"); }
    private RbacException storageError(String message) { return new RbacException(HttpStatus.BAD_GATEWAY, "NEWSPAPER_STORAGE_ERROR", message); }
}
