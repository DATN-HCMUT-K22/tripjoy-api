package com.tripjoy.api.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.MediaUploadResponse;
import com.tripjoy.api.service.IStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * MediaController — handles all media upload/delete operations.
 *
 * Upload flows:
 * A) Server-side: POST /upload/image or /upload/video (file via multipart)
 * B) Client-side: GET /sign → React Native uploads directly to Cloudinary
 *
 * Folder convention:
 * tripjoy/avatars/users/ → user avatars
 * tripjoy/avatars/groups/ → group avatars
 * tripjoy/posts/{creatorId}/ → post media
 * tripjoy/messages/{conversationId}/ → chat media
 */
@RestController
@RequestMapping(Endpoint.Media.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Media", description = "Upload, delete, and sign media files via Cloudinary")
public class MediaController {

    IStorageService storageService;

    // ─── Server-side uploads ───────────────────────────────────────────────────

    @Operation(
            summary = "Upload an image (server-side)",
            description =
                    """
			Uploads an image directly through the backend to Cloudinary.
			Accepts: JPG, PNG, WEBP, GIF, HEIC. Max size: 10 MB.
			Image is automatically converted to WebP/AVIF, resized to max 1080px width.
			Returns a secure HTTPS URL to store in the entity (avatarUrl, mediaUrls, etc.).
			""")
    @PostMapping(value = Endpoint.Media.UPLOAD_IMAGE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> uploadImage(
            @Parameter(
                            description = "Image file (JPG/PNG/WEBP/GIF/HEIC, max 10MB)",
                            required = true,
                            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                    @RequestPart("file")
                    MultipartFile file,
            @Parameter(description = "Target folder on Cloudinary (e.g. 'tripjoy/avatars/users')")
                    @RequestParam(value = "folder", defaultValue = "tripjoy/misc")
                    String folder) {

        MediaUploadResponse result = storageService.uploadImage(file, folder);
        return ApiResponse.<MediaUploadResponse>builder()
                .message("Image uploaded successfully")
                .data(result)
                .build();
    }

    @Operation(
            summary = "Upload a video (server-side)",
            description =
                    """
			Uploads a video directly through the backend to Cloudinary.
			Accepts: MP4, MOV, AVI, WEBM. Max size: 50 MB.
			Video is transcoded to MP4 with quality_auto.
			Returns a secure HTTPS URL to store in the entity (mediaUrls, etc.).
			""")
    @PostMapping(value = Endpoint.Media.UPLOAD_VIDEO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> uploadVideo(
            @Parameter(
                            description = "Video file (MP4/MOV/AVI/WEBM, max 50MB)",
                            required = true,
                            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                    @RequestPart("file")
                    MultipartFile file,
            @Parameter(description = "Target folder on Cloudinary (e.g. 'tripjoy/messages/conversationId')")
                    @RequestParam(value = "folder", defaultValue = "tripjoy/misc")
                    String folder) {

        MediaUploadResponse result = storageService.uploadVideo(file, folder);
        return ApiResponse.<MediaUploadResponse>builder()
                .message("Video uploaded successfully")
                .data(result)
                .build();
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Operation(
            summary = "Delete a media file",
            description = "Permanently removes a file from Cloudinary by its publicId.")
    @DeleteMapping(Endpoint.Media.DELETE)
    public ApiResponse<Void> deleteMedia(
            @Parameter(description = "Cloudinary public_id (e.g. 'tripjoy/avatars/users/abc123')", required = true)
                    @RequestParam("publicId")
                    String publicId,
            @Parameter(description = "Resource type: 'image' or 'video'")
                    @RequestParam(value = "resourceType", defaultValue = "image")
                    String resourceType) {

        storageService.deleteFile(publicId, resourceType);
        return ApiResponse.<Void>builder()
                .message("Media file deleted successfully")
                .build();
    }

    // ─── Client-side direct upload signature ──────────────────────────────────

    @Operation(
            summary = "Generate signed upload credentials (for React Native direct upload)",
            description =
                    """
			Generates a signed signature + timestamp that React Native can use to
			upload files directly to Cloudinary without routing through the backend.
			This avoids backend bandwidth usage for large files.

			Flow:
			1. React Native calls GET /api/v1/media/sign?folder=tripjoy/posts
			2. Backend returns signature, timestamp, apiKey, cloudName
			3. React Native uploads directly to https://api.cloudinary.com/v1_1/{cloudName}/image/upload
			4. React Native receives the secure_url from Cloudinary
			5. React Native includes this URL in POST /api/v1/posts body
			""")
    @GetMapping(Endpoint.Media.SIGN)
    public ApiResponse<Map<String, Object>> generateUploadSignature(
            @Parameter(description = "Target folder (e.g. 'tripjoy/posts')")
                    @RequestParam(value = "folder", defaultValue = "tripjoy/misc")
                    String folder,
            @Parameter(description = "Optional Cloudinary upload preset name")
                    @RequestParam(value = "uploadPreset", required = false)
                    String uploadPreset) {

        Map<String, Object> signature = storageService.generateUploadSignature(folder, uploadPreset);
        return ApiResponse.<Map<String, Object>>builder()
                .message("Upload signature generated")
                .data(signature)
                .build();
    }
}
