package com.sunrise.dental.model.enums;

/**
 * Patient gender.
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromString(String gender) {
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(gender)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + gender);
    }
}