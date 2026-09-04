package com.sunrise.dental.util;

import com.sunrise.dental.exception.ValidationException;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]{2,100}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$");

    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validate required field (not null and not empty).
     */
    public static void validateRequired(String field, String fieldName) throws ValidationException {
        if (field == null || field.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    /**
     * Validate required field (not null).
     */
    public static void validateRequired(Object field, String fieldName) throws ValidationException {
        if (field == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    /**
     * Validate patient name.
     */
    public static void validateName(String name) throws ValidationException {
        validateRequired(name, "Patient name");
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Patient name must be between 2-100 characters and contain only letters, spaces, dots, apostrophes, and hyphens.");
        }
    }

    /**
     * Validate contact number.
     */
    public static void validateContactNumber(String contactNumber) throws ValidationException {
        validateRequired(contactNumber, "Contact number");
        if (!PHONE_PATTERN.matcher(contactNumber).matches()) {
            throw new ValidationException("Contact number must be 10-15 digits.");
        }
    }

    /**
     * Validate email (optional).
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email != null && !email.trim().isEmpty()) {
            if (!EmailValidator.getInstance().isValid(email)) {
                throw new ValidationException("Please enter a valid email address.");
            }
        }
    }

    /**
     * Validate username.
     */
    public static void validateUsername(String username) throws ValidationException {
        validateRequired(username, "Username");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("Username must be 3-50 characters and contain only letters, numbers, and underscores.");
        }
    }

    /**
     * Validate password.
     */
    public static void validatePassword(String password) throws ValidationException {
        validateRequired(password, "Password");
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
    }

    /**
     * Validate appointment date (not in the past for new appointments).
     */
    public static void validateAppointmentDate(LocalDate date, boolean allowPast) throws ValidationException {
        validateRequired(date, "Appointment date");
        if (!allowPast && date.isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment date cannot be in the past.");
        }
    }

    /**
     * Validate appointment time.
     */
    public static void validateAppointmentTime(LocalTime time) throws ValidationException {
        validateRequired(time, "Appointment time");
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
            throw new ValidationException("Appointment time must be between 8:00 AM and 6:00 PM.");
        }
        if (time.getMinute() % 15 != 0) {
            throw new ValidationException("Appointment time must be in 15-minute intervals.");
        }
    }

    /**
     * Validate patient number format.
     */
    public static void validatePatientNumber(String patientNumber) throws ValidationException {
        validateRequired(patientNumber, "Patient number");
        if (!patientNumber.matches("^P\\d{4}$")) {
            throw new ValidationException("Patient number must be in format P0001.");
        }
    }

    /**
     * Validate appointment number format.
     */
    public static void validateAppointmentNumber(String appointmentNumber) throws ValidationException {
        validateRequired(appointmentNumber, "Appointment number");
        if (!appointmentNumber.matches("^A\\d{4}$")) {
            throw new ValidationException("Appointment number must be in format A0001.");
        }
    }

    /**
     * Validate ID (positive).
     */
    public static void validateId(int id, String entityName) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(entityName + " ID must be a positive number.");
        }
    }

    /**
     * Validate amount (positive).
     */
    public static void validateAmount(java.math.BigDecimal amount, String fieldName) throws ValidationException {
        if (amount == null) {
            throw new ValidationException(fieldName + " is required.");
        }
        if (amount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    /**
     * Validate discount (between 0 and subtotal).
     */
    public static void validateDiscount(java.math.BigDecimal discount, java.math.BigDecimal subtotal) throws ValidationException {
        if (discount == null) {
            return; // Discount can be null (treated as 0)
        }
        if (discount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("Discount cannot be negative.");
        }
        if (discount.compareTo(subtotal) > 0) {
            throw new ValidationException("Discount cannot exceed subtotal.");
        }
    }

    /**
     * Validate date range.
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) throws ValidationException {
        validateRequired(startDate, "Start date");
        validateRequired(endDate, "End date");
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date.");
        }
    }

    /**
     * Check if string is not empty.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Check if string is empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}