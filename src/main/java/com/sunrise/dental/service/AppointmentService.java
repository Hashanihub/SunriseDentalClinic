package com.sunrise.dental.service;

import com.sunrise.dental.dto.AppointmentRequestDTO;
import com.sunrise.dental.exception.AppointmentConflictException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Appointment service interface.
 */
public interface AppointmentService {

    /**
     * Create a new appointment.
     * @param request The appointment request
     * @return The created appointment
     * @throws ValidationException if validation fails
     * @throws AppointmentConflictException if dentist is not available
     * @throws ResourceNotFoundException if patient, dentist, or treatment not found
     * @throws DatabaseException if database error occurs
     */
    Appointment createAppointment(AppointmentRequestDTO request)
            throws ValidationException, AppointmentConflictException,
            ResourceNotFoundException, DatabaseException;

    /**
     * Get appointment by ID.
     * @param appointmentId The appointment ID
     * @return The appointment
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    Appointment getAppointmentById(int appointmentId)
            throws ResourceNotFoundException, DatabaseException;

    /**
     * Get appointment by appointment number.
     * @param appointmentNumber The appointment number
     * @return The appointment with details
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    Appointment getAppointmentByNumber(String appointmentNumber)
            throws ResourceNotFoundException, DatabaseException;

    /**
     * Get appointments by patient ID.
     * @param patientId The patient ID
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getAppointmentsByPatientId(int patientId) throws DatabaseException;

    /**
     * Get appointments by dentist ID.
     * @param dentistId The dentist ID
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getAppointmentsByDentistId(int dentistId) throws DatabaseException;

    /**
     * Get appointments by date.
     * @param date The date
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getAppointmentsByDate(LocalDate date) throws DatabaseException;

    /**
     * Get appointments by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate)
            throws DatabaseException;

    /**
     * Get appointments by status.
     * @param status The status
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status) throws DatabaseException;

    /**
     * Get today's appointments.
     * @return List of today's appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getTodayAppointments() throws DatabaseException;

    /**
     * Update appointment.
     * @param appointment The appointment to update
     * @return Updated appointment
     * @throws ValidationException if validation fails
     * @throws AppointmentConflictException if dentist is not available
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    Appointment updateAppointment(Appointment appointment)
            throws ValidationException, AppointmentConflictException,
            ResourceNotFoundException, DatabaseException;

    /**
     * Update appointment status.
     * @param appointmentId The appointment ID
     * @param status The new status
     * @return true if updated
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    boolean updateAppointmentStatus(int appointmentId, AppointmentStatus status)
            throws ResourceNotFoundException, DatabaseException;

    /**
     * Cancel appointment.
     * @param appointmentId The appointment ID
     * @param reason The cancellation reason
     * @return true if cancelled
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    boolean cancelAppointment(int appointmentId, String reason)
            throws ResourceNotFoundException, DatabaseException;

    /**
     * Reschedule appointment.
     *
     * @param appointmentId The appointment ID
     * @param newDate       The new date
     * @param newTime       The new time
     * @return The rescheduled appointment
     * @throws ResourceNotFoundException    if appointment not found
     * @throws AppointmentConflictException if dentist is not available
     * @throws ValidationException          if validation fails
     * @throws DatabaseException            if database error occurs
     */
    default Appointment rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newTime)
            throws ResourceNotFoundException, AppointmentConflictException,
            ValidationException, DatabaseException {
        return null;
    }

    /**
     * Check if dentist is available.
     * @param dentistId The dentist ID
     * @param date The date
     * @param time The time
     * @return true if available
     * @throws DatabaseException if database error occurs
     */
    boolean isDentistAvailable(int dentistId, LocalDate date, LocalTime time)
            throws DatabaseException;

    default Appointment rescheduleAppointment()
            throws ResourceNotFoundException, AppointmentConflictException,
            ValidationException, DatabaseException {
        return rescheduleAppointment(0, null, null);
    }

    /**
     * Get appointment statistics.
     * @return Appointment statistics
     * @throws DatabaseException if database error occurs
     */
    AppointmentStatistics getStatistics() throws DatabaseException;

    /**
     * Inner class for appointment statistics.
     */
    class AppointmentStatistics {
        private long totalAppointments;
        private long todayAppointments;
        private long scheduled;
        private long confirmed;
        private long completed;
        private long cancelled;
        private long noShow;

        // Getters and Setters
        public long getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }
        public long getTodayAppointments() { return todayAppointments; }
        public void setTodayAppointments(long todayAppointments) { this.todayAppointments = todayAppointments; }
        public long getScheduled() { return scheduled; }
        public void setScheduled(long scheduled) { this.scheduled = scheduled; }
        public long getConfirmed() { return confirmed; }
        public void setConfirmed(long confirmed) { this.confirmed = confirmed; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getCancelled() { return cancelled; }
        public void setCancelled(long cancelled) { this.cancelled = cancelled; }
        public long getNoShow() { return noShow; }
        public void setNoShow(long noShow) { this.noShow = noShow; }
    }
}