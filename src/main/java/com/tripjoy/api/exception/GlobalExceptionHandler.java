package com.tripjoy.api.exception;

import com.tripjoy.api.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";
    private static final String MAX_ATTRIBUTE = "max";
    private static final String VALUE_ATTRIBUTE = "value";

    // UNEXPECTED ERRORS (500)
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handleRuntimeException(Exception e) {
        // Log Error kèm StackTrace để dev debug (chỉ cho lỗi 500)
        log.error("Uncategorized Exception: ", e);

        return ResponseEntity
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                        .build());
    }


    // BUSINESS EXCEPTIONS (Custom App Logic)
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        // Chỉ log WARN cho lỗi nghiệp vụ
        log.warn("AppException: {}", errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }


    // VALIDATION EXCEPTION (@Valid, @NotNull, @Size...)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : "INVALID_KEY";

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            // 1. Map String message sang Enum ErrorCode
            errorCode = ErrorCode.valueOf(enumKey);

            // 2. Lấy attributes từ annotation (min, max, value...)
            var firstError = e.getBindingResult().getAllErrors().getFirst();
            var constraintViolation = firstError.unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

        } catch (IllegalArgumentException ex) {
            // Nếu message không phải là Key trong Enum -> Log warn và giữ nguyên lỗi mặc định
            // log.warn("Validation Key '{}' not found in ErrorCode Enum", enumKey);
        } catch (Exception ex) {
            // Fallback an toàn nếu không unwrap được
        }

        // 3. Map dynamic values ({min}, {max}) vào message
        String message = Objects.nonNull(attributes)
                ? mapAttribute(errorCode.getMessage(), attributes)
                : errorCode.getMessage();

        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(message)
                .build());
    }


    // SECURITY EXCEPTION
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build()
        );
    }

    // SPRING FRAMEWORK EXCEPTIONS (Common Issues)

    // JSON sai định dạng (VD: gửi chữ cái vào trường số)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleJsonException(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.INVALID_JSON.getCode())
                        .message(ErrorCode.INVALID_JSON.getMessage())
                        .build()
        );
    }

    // Lỗi gọi sai method (POST vào endpoint GET)
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.METHOD_NOT_ALLOWED.getCode())
                        .message(ErrorCode.METHOD_NOT_ALLOWED.getMessage())
                        .build()
        );
    }

    // Lỗi 404 Resource (Fix lỗi trả về 403 khi gọi sai URL)
    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleResourceNotFound(NoResourceFoundException e) {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                        .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                        .build()
        );
    }

    // Lỗi DB (Unique Constraint, Foreign Key...)
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleDbException(DataIntegrityViolationException e) {
        // Có thể phân tích e.getMessage() để trả về lỗi chi tiết hơn nếu muốn
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.CONSTRAINT_VIOLATION.getCode())
                        .message(ErrorCode.CONSTRAINT_VIOLATION.getMessage())
                        .build()
        );
    }



    // UTILITIES METHOD
    private String mapAttribute(String message, Map<String, Object> attributes) {
        String result = message;

        // Replace {min}
        if (attributes.containsKey(MIN_ATTRIBUTE)) {
            result = result.replace("{" + MIN_ATTRIBUTE + "}", String.valueOf(attributes.get(MIN_ATTRIBUTE)));
        }

        // Replace {max}
        if (attributes.containsKey(MAX_ATTRIBUTE)) {
            result = result.replace("{" + MAX_ATTRIBUTE + "}", String.valueOf(attributes.get(MAX_ATTRIBUTE)));
        }

        // Replace {value} (Thường dùng cho @Min, @Max)
        if (attributes.containsKey(VALUE_ATTRIBUTE)) {
            result = result.replace("{" + VALUE_ATTRIBUTE + "}", String.valueOf(attributes.get(VALUE_ATTRIBUTE)));
        }

        return result;
    }
}
