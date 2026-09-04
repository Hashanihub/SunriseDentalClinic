package com.sunrise.dental.model.enums;

/**
 * Appointment status values.
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled", "Appointment has been scheduled"),
    CONFIRMED("Confirmed", "Appointment has been confirmed"),
    COMPLETED("Completed", "Appointment has been completed"),
    CANCELLED("Cancelled", "Appointment has been cancelled"),
    NO_SHOW("No Show", "Patient did not show up");

    private final String displayName;
    private final String description;

    AppointmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static AppointmentStatus fromString(String status) {
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }

    public boolean isActive() {
        return this != CANCELLED && this != NO_SHOW;
    }
}