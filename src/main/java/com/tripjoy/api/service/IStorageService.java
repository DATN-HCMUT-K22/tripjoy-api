package com.tripjoy.api.service;

import com.tripjoy.api.dto.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Abstraction layer for cloud media storage.
 * Current implementation: Cloudinary.
 * Designed to be swappable with MinIO or AWS S3 in future.
 */
public interface IStorageService {

    /**
     * Upload an image file to cloud storage.
     * Automatically converts to WebP, resizes to max 1080px, applies quality_auto.
     *
     * @param file   the image file (JPG, PNG, WEBP, GIF, HEIC — max 10MB)
     * @param folder destination folder path (e.g. "tripjoy/avatars/users")
     * @return MediaUploadResponse with secure URL, publicId and dimensions
     */
    MediaUploadResponse uploadImage(MultipartFile file, String folder);

    /**
     * Upload a video file to cloud storage.
     * Transcodes to MP4, applies quality_auto.
     *
     * @param file   the video file (MP4, MOV, AVI — max 50MB)
     * @param folder destination folder path (e.g.
     *               "tripjoy/messages/conversationId")
     * @return MediaUploadResponse with secure URL, publicId and duration
     */
    MediaUploadResponse uploadVideo(MultipartFile file, String folder);

    /**
     * Delete a file from cloud storage by its public_id.
     *
     * @param publicId     the Cloudinary public_id (e.g.
     *                     "tripjoy/avatars/users/abc123")
     * @param resourceType "image" or "video"
     */
    void deleteFile(String publicId, String resourceType);

    /**
     * Generate a signed upload signature for client-side direct upload (React
     * Native).
     * Client uploads directly to Cloudinary without going through the backend.
     *
     * @param folder       target folder path
     * @param uploadPreset Cloudinary upload preset name (must be configured in
     *                     dashboard)
     * @return map containing: signature, timestamp, apiKey, cloudName, folder
     */
    Map<String, Object> generateUploadSignature(String folder, String uploadPreset);
}
