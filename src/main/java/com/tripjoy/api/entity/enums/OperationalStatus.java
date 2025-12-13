package com.tripjoy.api.entity.enums;


public enum OperationalStatus {
    ACTIVE,  // Location is currently active and operational
    CLOSED,  // Location is permanently closed
    TEMPORARILY_CLOSED, // Location is temporarily closed (may reopen)
    PERMANENTLY_CLOSED, // Location is permanently closed
    UNKNOWN // Operational status is unknown or not provided
}
