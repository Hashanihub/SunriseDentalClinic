package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Patient;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Patient entity.
 */
public interface PatientDAO {

    /**
     * Save a new patient.
     * @param patient The patient to save
     * @return The saved patient with generated ID
     * @throws DatabaseException if database error occurs
     */
    Patient save(Patient patient) throws DatabaseException;

    /**
     * Find patient by ID.
     * @param patientId The patient ID
     * @return Optional containing patient if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Patient> findById(int patientId) throws DatabaseException;

    /**
     * Find patient by patient number.
     * @param patientNumber The patient number
     * @return Optional containing patient if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Patient> findByPatientNumber(String patientNumber) throws DatabaseException;

    /**
     * Find patients by name (partial match).
     * @param name The name to search
     * @return List of matching patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> findByName(String name) throws DatabaseException;

    /**
     * Find patients by contact number.
     * @param contactNumber The contact number
     * @return Optional containing patient if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Patient> findByContactNumber(String contactNumber) throws DatabaseException;

    /**
     * Get all patients.
     * @return List of all patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> findAll() throws DatabaseException;

    /**
     * Get all active patients.
     * @return List of active patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> findAllActive() throws DatabaseException;

    /**
     * Update patient.
     * @param patient The patient to update
     * @return Updated patient
     * @throws DatabaseException if database error occurs
     */
    Patient update(Patient patient) throws DatabaseException;

    /**
     * Delete patient by ID (soft delete - set active to false).
     * @param patientId The patient ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int patientId) throws DatabaseException;

    /**
     * Check if patient number exists.
     * @param patientNumber The patient number
     * @return true if patient number exists
     * @throws DatabaseException if database error occurs
     */
    boolean patientNumberExists(String patientNumber) throws DatabaseException;

    /**
     * Check if contact number exists for another patient.
     * @param contactNumber The contact number
     * @param excludePatientId Patient ID to exclude from check
     * @return true if contact number exists
     * @throws DatabaseException if database error occurs
     */
    boolean contactNumberExists(String contactNumber, int excludePatientId) throws DatabaseException;

    /**
     * Get count of patients.
     * @return Total number of patients
     * @throws DatabaseException if database error occurs
     */
    long count() throws DatabaseException;

    /**
     * Get patients registered between dates.
     * @param startDate Start date
     * @param endDate End date
     * @return List of patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> findByRegistrationDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) throws DatabaseException;
}