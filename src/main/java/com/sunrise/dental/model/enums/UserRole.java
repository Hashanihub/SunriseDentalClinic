package com.sunrise.dental.model.enums;

/**
 * User roles for authentication and authorization.
 */
public enum UserRole {
    ADMIN("Admin", "Full system access"),
    RECEPTIONIST("Receptionist", "Patient and appointment management"),
    DENTIST("Dentist", "View appointments and update treatment status");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String role) {
        for (UserRole r : UserRole.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}