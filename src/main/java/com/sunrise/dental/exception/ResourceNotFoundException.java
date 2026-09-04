package com.sunrise.dental.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BaseException {

    private final String resourceType;
    private final String resourceId;

    // Constructor 1: With resource type and ID
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with ID " + resourceId + " not found",
                "NOTFOUND-001",
                resourceType + " not found. Please check the ID and try again.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    // Constructor 2: With message and user message
    public ResourceNotFoundException(String message, String userMessage, Throwable cause) {
        super(message, cause, "NOTFOUND-001", userMessage);
        this.resourceType = "Resource";
        this.resourceId = "Unknown";
    }

    // Constructor 3: Simple message only
    public ResourceNotFoundException(String message) {
        super(message, "NOTFOUND-001", message);
        this.resourceType = "Resource";
        this.resourceId = "Unknown";
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}