package com.sunrise.dental.util;

import com.sunrise.dental.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil class.
 */
class ValidationUtilTest {

    @Test
    void validateRequired_ShouldPass_WhenFieldIsNotNull() {
        assertDoesNotThrow(() -> ValidationUtil.validateRequired("test", "Field"));
        assertDoesNotThrow(() -> ValidationUtil.validateRequired(123, "Field"));
        assertDoesNotThrow(() -> ValidationUtil.validateRequired(LocalDate.now(), "Field"));
    }

    @Test
    void validateRequired_ShouldThrowException_WhenFieldIsNull() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRequired((String) null, "Field"));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRequired((Object) null, "Field"));
    }

    @Test
    void validateRequired_ShouldThrowException_WhenStringIsEmpty() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRequired("", "Field"));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateRequired("   ", "Field"));
    }

    @Test
    void validateName_ShouldPass_WhenNameIsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateName("John Doe"));
        assertDoesNotThrow(() -> ValidationUtil.validateName("Mary Jane-Smith"));
        assertDoesNotThrow(() -> ValidationUtil.validateName("Dr. O'Reilly"));
    }

    @Test
    void validateName_ShouldThrowException_WhenNameIsInvalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateName("J")); // Too short
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateName("John123")); // Contains numbers
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateName("")); // Empty
    }

    @Test
    void validateContactNumber_ShouldPass_WhenNumberIsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateContactNumber("0712345678"));
        assertDoesNotThrow(() -> ValidationUtil.validateContactNumber("0771234567"));
        assertDoesNotThrow(() -> ValidationUtil.validateContactNumber("0112345678"));
    }

    @Test
    void validateContactNumber_ShouldThrowException_WhenNumberIsInvalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateContactNumber("123")); // Too short
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateContactNumber("abcdefghij")); // Letters
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateContactNumber(null)); // Null
    }

    @Test
    void validateEmail_ShouldPass_WhenEmailIsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("john.doe@company.lk"));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user123@domain.org"));
    }

    @Test
    void validateEmail_ShouldPass_WhenEmailIsNullOrEmpty() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail(null));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail(""));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("   "));
    }

    @Test
    void validateEmail_ShouldThrowException_WhenEmailIsInvalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateEmail("invalid-email"));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateEmail("test@"));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateEmail("@example.com"));
    }

    @Test
    void validateAppointmentDate_ShouldPass_WhenDateIsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentDate(LocalDate.now().plusDays(1), false));
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentDate(LocalDate.now(), true));
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentDate(LocalDate.now().minusDays(1), true));
    }

    @Test
    void validateAppointmentDate_ShouldThrowException_WhenDateIsInPast() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateAppointmentDate(LocalDate.now().minusDays(1), false));
    }

    @Test
    void validateAppointmentTime_ShouldPass_WhenTimeIsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentTime(LocalTime.of(9, 0)));
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentTime(LocalTime.of(12, 30)));
        assertDoesNotThrow(() -> ValidationUtil.validateAppointmentTime(LocalTime.of(17, 45)));
    }

    @Test
    void validateAppointmentTime_ShouldThrowException_WhenTimeIsOutsideBusinessHours() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateAppointmentTime(LocalTime.of(7, 0))); // Before 8 AM
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateAppointmentTime(LocalTime.of(19, 0))); // After 6 PM
    }

    @Test
    void validateAppointmentTime_ShouldThrowException_WhenTimeIsNotIn15MinuteIntervals() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateAppointmentTime(LocalTime.of(9, 10)));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateAppointmentTime(LocalTime.of(14, 20)));
    }

    @Test
    void validateDateRange_ShouldPass_WhenRangeIsValid() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        assertDoesNotThrow(() -> ValidationUtil.validateDateRange(start, end));
    }

    @Test
    void validateDateRange_ShouldThrowException_WhenStartDateIsAfterEndDate() {
        LocalDate start = LocalDate.of(2024, 1, 31);
        LocalDate end = LocalDate.of(2024, 1, 1);
        assertThrows(ValidationException.class,
                () -> ValidationUtil.validateDateRange(start, end));
    }

    @Test
    void isNotEmpty_ShouldReturnTrue_WhenStringIsNotEmpty() {
        assertTrue(ValidationUtil.isNotEmpty("Hello"));
        assertTrue(ValidationUtil.isNotEmpty("  Hello  "));
    }

    @Test
    void isNotEmpty_ShouldReturnFalse_WhenStringIsEmpty() {
        assertFalse(ValidationUtil.isNotEmpty(null));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
    }

    @Test
    void isEmpty_ShouldReturnTrue_WhenStringIsEmpty() {
        assertTrue(ValidationUtil.isEmpty(null));
        assertTrue(ValidationUtil.isEmpty(""));
        assertTrue(ValidationUtil.isEmpty("   "));
    }

    @Test
    void isEmpty_ShouldReturnFalse_WhenStringIsNotEmpty() {
        assertFalse(ValidationUtil.isEmpty("Hello"));
        assertFalse(ValidationUtil.isEmpty("  Hello  "));
    }
}