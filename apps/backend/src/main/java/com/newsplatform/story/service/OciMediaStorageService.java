package com.newsplatform.story.service;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.story.entity.StoryMediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

/** Object Storage-backed story and advertisement media. Objects remain private and are streamed by the API. */
@Service
@ConditionalOnProperty(name = "app.media.storage-type", havingValue = "oci")
public class OciMediaStorageService implements MediaStorageService {
    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucket;
    private final String publicUrl;
    private final long maxImageSize;
    private final long maxVideoSize;

    public OciMediaStorageService(
            ObjectStorageClient client,
            @Value("${app.media.oci.namespace}") String namespace,
            @Value("${app.media.oci.bucket}") String bucket,
            @Value("${app.media.public-url}") String publicUrl,
            @Value("${app.media.max-image-upload-size:10485760}") long maxImageSize,
            @Value("${app.media.max-video-upload-size:52428800}") long maxVideoSize) {
        this.client = client;
        this.namespace = required(namespace, "OCI_OBJECT_STORAGE_NAMESPACE");
        this.bucket = required(bucket, "OCI_OBJECT_STORAGE_BUCKET");
        this.publicUrl = publicUrl.replaceAll("/$", "");
        this.maxImageSize = maxImageSize;
        this.maxVideoSize = maxVideoSize;
    }

    @Override
    public StoredMedia store(MultipartFile file) {
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
            String key = "media/" + UUID.randomUUID() + extension;
            try (InputStream input = file.getInputStream()) {
                PutObjectResponse ignored = client.putObject(PutObjectRequest.builder()
                        .namespaceName(namespace).bucketName(bucket).objectName(key)
                        .contentType(mime).contentLength(file.getSize()).putObjectBody(input).build());
            }
            return new StoredMedia(key, mime, file.getSize(), publicUrl + "/api/v1/media/" + key);
        } catch (IOException | BmcException exception) {
            throw storageError("The media could not be stored", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (!safeKey(storageKey)) return;
        try {
            client.deleteObject(DeleteObjectRequest.builder().namespaceName(namespace).bucketName(bucket).objectName(storageKey).build());
        } catch (BmcException ignored) {
            // Cleanup is best effort after a database transaction has failed.
        }
    }

    @Override
    public RetrievedMedia retrieve(String storageKey) {
        if (!safeKey(storageKey)) throw notFound();
        try {
            var response = client.getObject(GetObjectRequest.builder().namespaceName(namespace).bucketName(bucket).objectName(storageKey).build());
            String mime = response.getContentType() == null ? "application/octet-stream" : response.getContentType();
            return new RetrievedMedia(new InputStreamResource(response.getInputStream()), mime);
        } catch (BmcException exception) {
            throw new RbacException(exception.getStatusCode() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY,
                    "MEDIA_NOT_FOUND", "Media not found");
        }
    }

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
    private boolean safeKey(String key) { return key != null && !key.isBlank() && !key.contains("\\") && !key.contains("..") && key.startsWith("media/"); }
    private String required(String value, String name) { if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required when MEDIA_STORAGE_TYPE=oci"); return value; }
    private RbacException bad(String code, String message) { return new RbacException(HttpStatus.BAD_REQUEST, code, message); }
    private RbacException notFound() { return new RbacException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND", "Media not found"); }
    private RbacException storageError(String message, Exception cause) { return new RbacException(HttpStatus.BAD_GATEWAY, "MEDIA_STORAGE_ERROR", message); }
}
