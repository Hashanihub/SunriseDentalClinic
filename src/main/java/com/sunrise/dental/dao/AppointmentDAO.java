package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Appointment entity.
 */
public interface AppointmentDAO {

    /**
     * Save a new appointment.
     * @param appointment The appointment to save
     * @return The saved appointment with generated ID
     * @throws DatabaseException if database error occurs
     */
    Appointment save(Appointment appointment) throws DatabaseException;

    /**
     * Find appointment by ID.
     * @param appointmentId The appointment ID
     * @return Optional containing appointment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Appointment> findById(int appointmentId) throws DatabaseException;

    /**
     * Find appointment by appointment number.
     * @param appointmentNumber The appointment number
     * @return Optional containing appointment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Appointment> findByAppointmentNumber(String appointmentNumber) throws DatabaseException;

    /**
     * Find appointments by patient ID.
     * @param patientId The patient ID
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByPatientId(int patientId) throws DatabaseException;

    /**
     * Find appointments by dentist ID.
     * @param dentistId The dentist ID
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByDentistId(int dentistId) throws DatabaseException;

    /**
     * Find appointments by dentist ID and date.
     * @param dentistId The dentist ID
     * @param date The date
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) throws DatabaseException;

    /**
     * Find appointments by date.
     * @param date The date
     * @return List of appointments for the date
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByDate(LocalDate date) throws DatabaseException;

    /**
     * Find appointments by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Find appointments by status.
     * @param status The appointment status
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findByStatus(AppointmentStatus status) throws DatabaseException;

    /**
     * Find today's appointments.
     * @return List of today's appointments
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> findTodayAppointments() throws DatabaseException;

    /**
     * Update appointment.
     * @param appointment The appointment to update
     * @return Updated appointment
     * @throws DatabaseException if database error occurs
     */
    Appointment update(Appointment appointment) throws DatabaseException;

    /**
     * Update appointment status.
     * @param appointmentId The appointment ID
     * @param status The new status
     * @return true if updated successfully
     * @throws DatabaseException if database error occurs
     */
    boolean updateStatus(int appointmentId, AppointmentStatus status) throws DatabaseException;

    /**
     * Cancel appointment.
     * @param appointmentId The appointment ID
     * @param reason The cancellation reason
     * @return true if cancelled successfully
     * @throws DatabaseException if database error occurs
     */
    boolean cancelAppointment(int appointmentId, String reason) throws DatabaseException;

    /**
     * Delete appointment by ID (soft delete).
     * @param appointmentId The appointment ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int appointmentId) throws DatabaseException;

    /**
     * Check if dentist is available at date/time.
     * @param dentistId The dentist ID
     * @param date The date
     * @param time The time
     * @return true if available
     * @throws DatabaseException if database error occurs
     */
    boolean isDentistAvailable(int dentistId, LocalDate date, String time) throws DatabaseException;

    /**
     * Get appointment count by status.
     * @param status The status
     * @return Count of appointments
     * @throws DatabaseException if database error occurs
     */
    long countByStatus(AppointmentStatus status) throws DatabaseException;

    /**
     * Get today's appointment count.
     * @return Count of today's appointments
     * @throws DatabaseException if database error occurs
     */
    long countTodayAppointments() throws DatabaseException;
}