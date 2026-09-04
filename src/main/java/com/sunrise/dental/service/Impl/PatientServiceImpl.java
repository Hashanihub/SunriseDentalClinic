package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.Impl.PatientDAOImpl;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.service.PatientService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PatientService interface.
 */
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LogManager.getLogger(PatientServiceImpl.class);
    private final PatientDAO patientDAO;

    public PatientServiceImpl() {
        this.patientDAO = new PatientDAOImpl();
    }

    @Override
    public Patient registerPatient(Patient patient) throws ValidationException, DatabaseException {
        logger.info("Registering new patient: {}", patient.getFullName());

        // Validate patient data
        validatePatient(patient);

        // Check if contact number already exists
        if (patientDAO.contactNumberExists(patient.getContactNumber(), 0)) {
            throw new ValidationException("Contact number already registered. Please use a different number.");
        }

        // Set registration date if not set
        if (patient.getRegistrationDate() == null) {
            patient.setRegistrationDate(LocalDate.now());
        }

        // Save patient
        Patient saved = patientDAO.save(patient);
        logger.info("Patient registered successfully: {}", saved.getPatientNumber());
        return saved;
    }

    @Override
    public Patient getPatientById(int patientId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(patientId, "Patient");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Patient> patient = patientDAO.findById(patientId);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient", String.valueOf(patientId));
        }
        return patient.get();
    }

    @Override
    public Patient getPatientByNumber(String patientNumber) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validatePatientNumber(patientNumber);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Patient> patient = patientDAO.findByPatientNumber(patientNumber);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient", patientNumber);
        }
        return patient.get();
    }

    @Override
    public List<Patient> searchPatientsByName(String name) throws DatabaseException {
        if (name == null || name.trim().isEmpty()) {
            return patientDAO.findAllActive();
        }
        return patientDAO.findByName(name.trim());
    }

    @Override
    public Patient getPatientByContactNumber(String contactNumber) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateContactNumber(contactNumber);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Patient> patient = patientDAO.findByContactNumber(contactNumber);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient", "contact number: " + contactNumber);
        }
        return patient.get();
    }

    @Override
    public List<Patient> getAllPatients() throws DatabaseException {
        return patientDAO.findAll();
    }

    @Override
    public List<Patient> getActivePatients() throws DatabaseException {
        return patientDAO.findAllActive();
    }

    @Override
    public Patient updatePatient(Patient patient) throws ValidationException, ResourceNotFoundException, DatabaseException {
        logger.info("Updating patient: {}", patient.getPatientNumber());

        // Validate patient exists
        Optional<Patient> existing = patientDAO.findById(patient.getPatientId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Patient", String.valueOf(patient.getPatientId()));
        }

        // Validate patient data
        validatePatient(patient);

        // Check if contact number exists for another patient
        if (patientDAO.contactNumberExists(patient.getContactNumber(), patient.getPatientId())) {
            throw new ValidationException("Contact number already registered to another patient.");
        }

        Patient updated = patientDAO.update(patient);
        logger.info("Patient updated successfully: {}", updated.getPatientNumber());
        return updated;
    }

    @Override
    public boolean deactivatePatient(int patientId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(patientId, "Patient");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        Optional<Patient> patient = patientDAO.findById(patientId);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient", String.valueOf(patientId));
        }

        boolean deactivated = patientDAO.delete(patientId);
        logger.info("Patient deactivated: {}", patientId);
        return deactivated;
    }

    @Override
    public long getPatientCount() throws DatabaseException {
        return patientDAO.count();
    }

    @Override
    public List<Patient> getPatientsByRegistrationDate(LocalDate startDate, LocalDate endDate)
            throws DatabaseException, ValidationException {
        ValidationUtil.validateDateRange(startDate, endDate);
        return patientDAO.findByRegistrationDateRange(startDate, endDate);
    }

    /**
     * Validate patient data.
     */
    private void validatePatient(Patient patient) throws ValidationException {
        ValidationUtil.validateRequired(patient, "Patient");
        ValidationUtil.validateName(patient.getFullName());
        ValidationUtil.validateContactNumber(patient.getContactNumber());

        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            ValidationUtil.validateEmail(patient.getEmail());
        }
    }
}