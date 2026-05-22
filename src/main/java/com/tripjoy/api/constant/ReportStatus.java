package com.tripjoy.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status of a content report
 */
@Getter
@AllArgsConstructor
public enum ReportStatus {
    PENDING("Report submitted, awaiting review"),
    PROCESSED("Report reviewed and action taken"),
    DISMISSED("Report reviewed and dismissed");

    private final String description;

    /**
     * Validate if a string is a valid report status
     */
    public static boolean isValid(String status) {
        if (status == null) return false;
        try {
            ReportStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get enum from string (case-insensitive)
     */
    public static ReportStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Report status cannot be null");
        }
        return ReportStatus.valueOf(status.toUpperCase());
    }
}
