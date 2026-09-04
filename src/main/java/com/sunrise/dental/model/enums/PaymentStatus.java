package com.sunrise.dental.model.enums;

/**
 * Payment status values.
 */
public enum PaymentStatus {
    PAID("Paid", "Full payment received"),
    UNPAID("Unpaid", "No payment received"),
    PARTIAL("Partial", "Partial payment received");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus fromString(String status) {
        for (PaymentStatus s : PaymentStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + status);
    }
}