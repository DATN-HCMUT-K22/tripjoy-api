package com.tripjoy.api.service.impl;

import java.io.IOException;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tripjoy.api.dto.response.MediaUploadResponse;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.service.IStorageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryStorageService implements IStorageService {

    Cloudinary cloudinary;

    // ─── Allowed MIME types ───────────────────────────────────────────────────
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "image/heic", "image/heif");

    private static final Set<String> ALLOWED_VIDEO_TYPES =
            Set.of("video/mp4", "video/quicktime", "video/x-msvideo", "video/webm");

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10 MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024L; // 50 MB

    // ─── Upload Image ─────────────────────────────────────────────────────────
    @Override
    public MediaUploadResponse uploadImage(MultipartFile file, String folder) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "Image");

        try {
            Map<?, ?> uploadResult = cloudinary
                    .uploader()
                    .upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", folder,
                                    "resource_type", "image",
                                    "quality", "auto",
                                    "fetch_format", "auto", // serves WebP/AVIF based on browser support
                                    "width", 1080,
                                    "crop", "limit" // shrinks if wider than 1080, never upscales
                                    ));

            log.info("Image uploaded to Cloudinary: publicId={}", uploadResult.get("public_id"));
            return mapToResponse(uploadResult);

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error during image upload to Cloudinary: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }

    // ─── Upload Video ─────────────────────────────────────────────────────────
    @Override
    public MediaUploadResponse uploadVideo(MultipartFile file, String folder) {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "Video");

        try {
            Map<?, ?> uploadResult = cloudinary
                    .uploader()
                    .uploadLarge(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", folder,
                                    "resource_type", "video",
                                    "quality", "auto",
                                    "format", "mp4" // normalize all videos to mp4
                                    ));

            log.info("Video uploaded to Cloudinary: publicId={}", uploadResult.get("public_id"));
            return mapToResponse(uploadResult);

        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary: {}", e.getMessage());
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error during video upload to Cloudinary: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }

    // ─── Delete File ──────────────────────────────────────────────────────────
    @Override
    public void deleteFile(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_MEDIA_PUBLIC_ID);
        }
        try {
            Map<?, ?> result =
                    cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

            String resultStr = String.valueOf(result.get("result"));
            if (!"ok".equals(resultStr)) {
                log.warn("Cloudinary delete returned non-ok result for publicId={}: {}", publicId, resultStr);
            } else {
                log.info("Cloudinary file deleted: publicId={}", publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete from Cloudinary: {}", e.getMessage());
            throw new AppException(ErrorCode.MEDIA_DELETE_FAILED);
        }
    }

    // ─── Generate Signed Upload URL (for React Native direct upload) ──────────
    @Override
    public Map<String, Object> generateUploadSignature(String folder, String uploadPreset) {
        long timestamp = System.currentTimeMillis() / 1000L;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("folder", folder);
        params.put("timestamp", timestamp);
        if (uploadPreset != null && !uploadPreset.isBlank()) {
            params.put("upload_preset", uploadPreset);
        }

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("api_key", cloudinary.config.apiKey);
        response.put("cloud_name", cloudinary.config.cloudName);
        response.put("folder", folder);
        if (uploadPreset != null && !uploadPreset.isBlank()) {
            response.put("upload_preset", uploadPreset);
        }
        return response;
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MEDIA_FILE_EMPTY);
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.MEDIA_UNSUPPORTED_TYPE);
        }
        if (file.getSize() > maxSize) {
            throw new AppException(ErrorCode.MEDIA_FILE_TOO_LARGE);
        }
        log.debug(
                "{} file validation passed: name={}, type={}, size={} bytes",
                label,
                file.getOriginalFilename(),
                contentType,
                file.getSize());
    }

    private MediaUploadResponse mapToResponse(Map<?, ?> result) {
        return MediaUploadResponse.builder()
                .url(String.valueOf(result.get("url")))
                .secureUrl(String.valueOf(result.get("secure_url")))
                .publicId(String.valueOf(result.get("public_id")))
                .format(String.valueOf(result.get("format")))
                .resourceType(String.valueOf(result.get("resource_type")))
                .width(result.get("width") != null ? ((Number) result.get("width")).intValue() : null)
                .height(result.get("height") != null ? ((Number) result.get("height")).intValue() : null)
                .bytes(result.get("bytes") != null ? ((Number) result.get("bytes")).longValue() : null)
                .duration(result.get("duration") != null ? ((Number) result.get("duration")).doubleValue() : null)
                .build();
    }
}
