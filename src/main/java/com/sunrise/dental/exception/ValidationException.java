package com.sunrise.dental.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, "VAL-001", message);
    }

    public ValidationException(String message, String userMessage) {
        super(message, "VAL-001", userMessage);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, "VAL-001", message);
    }
}