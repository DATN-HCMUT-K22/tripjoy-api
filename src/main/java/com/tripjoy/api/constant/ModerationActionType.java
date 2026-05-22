package com.tripjoy.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Types of moderation actions that can be taken on users
 */
@Getter
@AllArgsConstructor
public enum ModerationActionType {
    BAN_USER("Permanently ban user account", true),
    SUSPEND_USER("Temporarily suspend user account", true),
    WARN_USER("Issue warning to user (no account lock)", false);

    private final String description;
    private final boolean locksAccount;

    /**
     * Check if this action type should lock the user account
     */
    public boolean shouldLockAccount() {
        return locksAccount;
    }

    /**
     * Validate if a string is a valid moderation action type
     */
    public static boolean isValid(String actionType) {
        if (actionType == null) return false;
        try {
            ModerationActionType.valueOf(actionType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get enum from string (case-insensitive)
     */
    public static ModerationActionType fromString(String actionType) {
        if (actionType == null) {
            throw new IllegalArgumentException("Moderation action type cannot be null");
        }
        return ModerationActionType.valueOf(actionType.toUpperCase());
    }
}
