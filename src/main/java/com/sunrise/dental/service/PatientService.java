package com.sunrise.dental.service;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Patient;

import java.time.LocalDate;
import java.util.List;

/**
 * Patient service interface.
 */
public interface PatientService {

    /**
     * Register a new patient.
     * @param patient The patient to register
     * @return The registered patient
     * @throws ValidationException if validation fails
     * @throws DatabaseException if database error occurs
     */
    Patient registerPatient(Patient patient) throws ValidationException, DatabaseException;

    /**
     * Get patient by ID.
     * @param patientId The patient ID
     * @return The patient
     * @throws ResourceNotFoundException if patient not found
     * @throws DatabaseException if database error occurs
     */
    Patient getPatientById(int patientId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get patient by patient number.
     * @param patientNumber The patient number
     * @return The patient
     * @throws ResourceNotFoundException if patient not found
     * @throws DatabaseException if database error occurs
     */
    Patient getPatientByNumber(String patientNumber) throws ResourceNotFoundException, DatabaseException;

    /**
     * Search patients by name.
     * @param name The name to search
     * @return List of matching patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> searchPatientsByName(String name) throws DatabaseException;

    /**
     * Find patient by contact number.
     * @param contactNumber The contact number
     * @return The patient
     * @throws ResourceNotFoundException if patient not found
     * @throws DatabaseException if database error occurs
     */
    Patient getPatientByContactNumber(String contactNumber) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get all patients.
     * @return List of all patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> getAllPatients() throws DatabaseException;

    /**
     * Get all active patients.
     * @return List of active patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> getActivePatients() throws DatabaseException;

    /**
     * Update patient.
     * @param patient The patient to update
     * @return Updated patient
     * @throws ValidationException if validation fails
     * @throws ResourceNotFoundException if patient not found
     * @throws DatabaseException if database error occurs
     */
    Patient updatePatient(Patient patient) throws ValidationException, ResourceNotFoundException, DatabaseException;

    /**
     * Deactivate patient (soft delete).
     * @param patientId The patient ID
     * @return true if deactivated
     * @throws ResourceNotFoundException if patient not found
     * @throws DatabaseException if database error occurs
     */
    boolean deactivatePatient(int patientId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get total patient count.
     * @return Total number of patients
     * @throws DatabaseException if database error occurs
     */
    long getPatientCount() throws DatabaseException;

    /**
     * Get patients registered in date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of patients
     * @throws DatabaseException if database error occurs
     * @throws ValidationException if date range is invalid
     */
    List<Patient> getPatientsByRegistrationDate(LocalDate startDate, LocalDate endDate)
            throws DatabaseException, ValidationException;
}