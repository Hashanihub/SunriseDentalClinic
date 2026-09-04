package com.sunrise.dental.exception;

/**
 * Base exception class for the Sunrise Dental Clinic Management System.
 * All custom exceptions should extend this class.
 */
public abstract class BaseException extends Exception {

    private final String errorCode;
    private final String userMessage;

    public BaseException(String message) {
        super(message);
        this.errorCode = "SYS-ERR-001";
        this.userMessage = "An unexpected error occurred. Please contact support.";
    }

    public BaseException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public BaseException(String message, Throwable cause, String errorCode, String userMessage) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}