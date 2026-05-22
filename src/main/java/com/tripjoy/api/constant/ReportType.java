package com.tripjoy.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Types of content reports that users can submit
 */
@Getter
@AllArgsConstructor
public enum ReportType {
    SPAM("Spam or scam content"),
    HARASSMENT("Harassment or bullying"),
    INAPPROPRIATE("Inappropriate or offensive content"),
    MISINFORMATION("False information or misinformation"),
    COPYRIGHT("Copyright violation"),
    OTHER("Other reason");

    private final String description;

    /**
     * Validate if a string is a valid report type
     */
    public static boolean isValid(String type) {
        if (type == null) return false;
        try {
            ReportType.valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get enum from string (case-insensitive)
     */
    public static ReportType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Report type cannot be null");
        }
        return ReportType.valueOf(type.toUpperCase());
    }
}
