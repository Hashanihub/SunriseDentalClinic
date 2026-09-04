package com.sunrise.dental.exception;

/**
 * Exception thrown when a database operation fails.
 */
public class DatabaseException extends BaseException {

    public DatabaseException(String message) {
        super(message, "DB-001", "A database error occurred. Please try again later.");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, "DB-001", "A database error occurred. Please try again later.");
    }

    public DatabaseException(String message, String userMessage, Throwable cause) {
        super(message, cause, "DB-001", userMessage);
    }
}