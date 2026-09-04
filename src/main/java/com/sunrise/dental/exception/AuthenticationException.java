package com.sunrise.dental.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message, "AUTH-001", "Invalid username or password");
    }

    public AuthenticationException(String message, String userMessage) {
        super(message, "AUTH-001", userMessage);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, "AUTH-001", "Invalid username or password");
    }
}