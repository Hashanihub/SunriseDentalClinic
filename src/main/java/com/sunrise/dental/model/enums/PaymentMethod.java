package com.sunrise.dental.model.enums;

/**
 * Payment methods.
 */
public enum PaymentMethod {
    CASH("Cash", "Cash payment"),
    CARD("Card", "Credit/Debit card"),
    BANK_TRANSFER("Bank Transfer", "Bank transfer payment"),
    INSURANCE("Insurance", "Insurance claim"),
    OTHER("Other", "Other payment method");

    private final String displayName;
    private final String description;

    PaymentMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentMethod fromString(String method) {
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.name().equalsIgnoreCase(method)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + method);
    }
}