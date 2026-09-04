package com.sunrise.dental.model.enums;

/**
 * Dentist availability status.
 */
public enum AvailabilityStatus {
    AVAILABLE("Available", "Available for appointments"),
    BUSY("Busy", "Currently busy with patients"),
    ON_LEAVE("On Leave", "On leave/not available"),
    INACTIVE("Inactive", "No longer active");

    private final String displayName;
    private final String description;

    AvailabilityStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static AvailabilityStatus fromString(String status) {
        for (AvailabilityStatus s : AvailabilityStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown availability status: " + status);
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }
}