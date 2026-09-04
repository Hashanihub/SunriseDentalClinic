package com.sunrise.dental.exception;

/**
 * Exception thrown when there is a conflict with appointment scheduling.
 */
public class AppointmentConflictException extends BaseException {

    private final String dentistName;
    private final String dateTime;

    // Constructor 1: With dentist name and date time
    public AppointmentConflictException(String dentistName, String dateTime) {
        super("Dentist " + dentistName + " already has an appointment at " + dateTime,
                "CONFLICT-001",
                "This dentist already has an appointment at the selected date and time.");
        this.dentistName = dentistName;
        this.dateTime = dateTime;
    }

    // Constructor 2: With message and user message
    public AppointmentConflictException(String message, String userMessage, Throwable cause) {
        super(message, cause, "CONFLICT-001", userMessage);
        this.dentistName = "Unknown";
        this.dateTime = "Unknown";
    }

    // Constructor 3: Simple message only
    public AppointmentConflictException(String message) {
        super(message, "CONFLICT-001", message);
        this.dentistName = "Unknown";
        this.dateTime = "Unknown";
    }

    public String getDentistName() {
        return dentistName;
    }

    public String getDateTime() {
        return dateTime;
    }
}