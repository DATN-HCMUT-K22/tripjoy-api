package com.tripjoy.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // --- 1. COMMON & SYSTEM ERRORS (1000 - 1999) ---
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request input", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1003, "Resource not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(1004, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    INVALID_JSON(1005, "Invalid JSON format", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1006, "Missing required parameter", HttpStatus.BAD_REQUEST),

    // --- 2. AUTHENTICATION & USER (2000 - 2999) ---
    // 401 Unauthorized: Chưa đăng nhập hoặc token sai
    UNAUTHENTICATED(2001, "You need to login to access this resource", HttpStatus.UNAUTHORIZED),
    // 403 Forbidden: Đã đăng nhập nhưng không có quyền
    UNAUTHORIZED(2002, "You do not have permission", HttpStatus.FORBIDDEN),

    USER_EXISTED(2003, "User already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2004, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(2005, "Username already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(2006, "User not found", HttpStatus.NOT_FOUND),
    USER_LOCKED(2007, "User account is locked", HttpStatus.FORBIDDEN),
    USER_NOT_VERIFIED(2008, "User email is not verified", HttpStatus.FORBIDDEN),

    ROLE_NOT_FOUND(2009, "Role not found", HttpStatus.INTERNAL_SERVER_ERROR), // Lỗi cấu hình hệ thống

    // Validation Messages (Có thể dùng placeholder để replace động)
    INVALID_USERNAME(2010, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2011, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),

    // --- 3. GROUP & TRIP DOMAIN (3000 - 3999) ---
    GROUP_NOT_FOUND(3001, "Group trip not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_IN_GROUP(3002, "User is already a member of this group", HttpStatus.BAD_REQUEST),
    USER_NOT_IN_GROUP(3003, "User is not a member of this group", HttpStatus.BAD_REQUEST),
    ONLY_LEADER_ALLOWED(3004, "Only group leader can perform this action", HttpStatus.FORBIDDEN),
    GROUP_FULL(3005, "Group has reached maximum capacity", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_GROUP(3006, "Cannot delete group with active members or itineraries", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND(3007, "Member not found in this group", HttpStatus.NOT_FOUND),
    CANNOT_REMOVE_YOURSELF(3008, "You cannot remove yourself from the group", HttpStatus.BAD_REQUEST),
    CANNOT_REMOVE_LEADER(3009, "Cannot remove the group leader", HttpStatus.FORBIDDEN),
    LEADER_CANNOT_LEAVE(3010, "Leader cannot leave group. Transfer leadership first", HttpStatus.BAD_REQUEST),
    CANNOT_CHANGE_LEADER_ROLE(3011, "Cannot change the leader's role", HttpStatus.FORBIDDEN),
    CANNOT_ASSIGN_LEADER_ROLE(
            3012, "Cannot assign LEADER role. Use transfer leadership instead", HttpStatus.BAD_REQUEST),
    CANNOT_TRANSFER_TO_YOURSELF(3013, "Cannot transfer leadership to yourself", HttpStatus.BAD_REQUEST),
    GROUP_NOT_DELETED(3014, "Group is not deleted", HttpStatus.BAD_REQUEST),
    SUGGESTION_NOT_FOUND(3015, "Location suggestion not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_OTHERS_SUGGESTION(3016, "You can only delete your own suggestions", HttpStatus.FORBIDDEN),

    // --- 4. CHAT & CONVERSATION DOMAIN (4000 - 4999) ---
    CONVERSATION_NOT_FOUND(4001, "Conversation not found", HttpStatus.NOT_FOUND),
    MESSAGE_NOT_FOUND(4002, "Message not found", HttpStatus.NOT_FOUND),
    USER_NOT_IN_CONVERSATION(4003, "User is not a participant of this conversation", HttpStatus.FORBIDDEN),
    CANNOT_MESSAGE_STRANGER(4004, "Cannot send message to stranger directly", HttpStatus.FORBIDDEN),
    SOCKET_ERROR(4005, "Real-time service error", HttpStatus.INTERNAL_SERVER_ERROR),
    MESSAGE_ALREADY_LIKED(4006, "You have already liked this message", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_LIKED(4007, "You have not liked this message", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_DIRECT_CHAT_NAME(4008, "Cannot update name of direct conversation", HttpStatus.BAD_REQUEST),
    MESSAGE_ALREADY_PINNED(4009, "Message is already pinned", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_PINNED(4010, "Message is not pinned", HttpStatus.BAD_REQUEST),
    PIN_LIMIT_EXCEEDED(4011, "Cannot pin more than 50 messages in a conversation", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_IN_CONVERSATION(4012, "Message does not belong to this conversation", HttpStatus.BAD_REQUEST),
    CANNOT_SELF_DIRECT_MESSAGE(4013, "Cannot create a direct conversation with yourself", HttpStatus.BAD_REQUEST),

    // --- 5. FILE & MEDIA (5000 - 5999) ---
    FILE_TOO_LARGE(5001, "File size exceeds the limit", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_TYPE(5002, "Unsupported file type", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(5003, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_FILE_EMPTY(5004, "Media file must not be empty", HttpStatus.BAD_REQUEST),
    MEDIA_UNSUPPORTED_TYPE(
            5005, "Unsupported media type. Allowed: JPG, PNG, WEBP, GIF, HEIC, MP4, MOV, AVI", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_TOO_LARGE(5006, "Media file exceeds maximum allowed size", HttpStatus.PAYLOAD_TOO_LARGE),
    MEDIA_UPLOAD_FAILED(5007, "Failed to upload media to cloud storage", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_DELETE_FAILED(5008, "Failed to delete media from cloud storage", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_MEDIA_PUBLIC_ID(5009, "Invalid media public ID", HttpStatus.BAD_REQUEST),

    // --- 6. DATABASE ERRORS (6000 - 6999) ---
    DATABASE_ERROR(6001, "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CONSTRAINT_VIOLATION(6002, "Database constraint violation", HttpStatus.CONFLICT),
    DUPLICATE_RESOURCE(6003, "Resource already exists", HttpStatus.CONFLICT),
    REFERENCED_RESOURCE(6004, "Cannot delete: Resource is being referenced", HttpStatus.CONFLICT),

    // --- 7. LOCATION DOMAIN (7000 - 7999) ---
    LOCATION_NOT_FOUND(7001, "Location not found", HttpStatus.NOT_FOUND),
    LOCATION_IN_USE(7002, "Cannot delete location: It is being used in group suggestions", HttpStatus.CONFLICT),
    INVALID_COORDINATES(7003, "Invalid latitude or longitude values", HttpStatus.BAD_REQUEST),
    MAPBOX_API_ERROR(7004, "Mapbox API request failed", HttpStatus.BAD_GATEWAY),
    INVALID_LOCATION_INPUT(7005, "Must provide either location_id OR location_data, but not both", HttpStatus.BAD_REQUEST),
    INVALID_LOCATION_TYPE(7006, "Invalid or unsupported location type", HttpStatus.BAD_REQUEST),
    LOCATION_TYPE_REQUIRED(7007, "Location type is required for this operation", HttpStatus.BAD_REQUEST),
    GOOGLE_MAPS_API_ERROR(7008, "Google Maps API request failed", HttpStatus.BAD_GATEWAY),
    LOCATION_SEEDING_FAILED(7009, "Location seeding process encountered an error", HttpStatus.INTERNAL_SERVER_ERROR),


    // --- 8. NOTIFICATION DOMAIN (8000 - 8999) ---
    NOTIF_NOT_FOUND(8001, "Notification not found", HttpStatus.NOT_FOUND),

    // --- 9. AI & EXTERNAL SERVICES (9000 - 9999) ---
    AI_SERVICE_UNAVAILABLE(9001, "AI Service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    AI_GENERATION_FAILED(9002, "AI failed to generate itinerary items", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_INVALID_RESPONSE(9003, "AI returned an invalid or malformed response", HttpStatus.BAD_GATEWAY),
    AI_REQUEST_TIMEOUT(9004, "AI generation request timed out", HttpStatus.GATEWAY_TIMEOUT),

    // --- 10. POST DOMAIN (10000 - 10999) ---
    POST_NOT_FOUND(10001, "Post not found", HttpStatus.NOT_FOUND),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
