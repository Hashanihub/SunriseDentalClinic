package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.DentistDAO;
import com.sunrise.dental.dao.Impl.DentistDAOImpl;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.enums.AvailabilityStatus;
import com.sunrise.dental.service.DentistService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DentistService interface.
 */
public class DentistServiceImpl implements DentistService {

    private static final Logger logger = LogManager.getLogger(DentistServiceImpl.class);
    private final DentistDAO dentistDAO;

    public DentistServiceImpl() {
        this.dentistDAO = new DentistDAOImpl();
    }

    @Override
    public Dentist addDentist(Dentist dentist) throws ValidationException, DatabaseException {
        logger.info("Adding new dentist: {}", dentist.getFullName());

        validateDentist(dentist);

        // Set default values
        if (dentist.getAvailabilityStatus() == null) {
            dentist.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }
        if (dentist.getConsultationFee() == null) {
            dentist.setConsultationFee(BigDecimal.ZERO);
        }

        Dentist saved = dentistDAO.save(dentist);
        logger.info("Dentist added successfully: {}", saved.getDentistNumber());
        return saved;
    }

    @Override
    public Dentist getDentistById(int dentistId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(dentistId, "Dentist");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Dentist> dentist = dentistDAO.findById(dentistId);
        if (dentist.isEmpty()) {
            throw new ResourceNotFoundException("Dentist", String.valueOf(dentistId));
        }
        return dentist.get();
    }

    @Override
    public Dentist getDentistByNumber(String dentistNumber) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateRequired(dentistNumber, "Dentist number");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Dentist> dentist = dentistDAO.findByDentistNumber(dentistNumber);
        if (dentist.isEmpty()) {
            throw new ResourceNotFoundException("Dentist", dentistNumber);
        }
        return dentist.get();
    }

    @Override
    public List<Dentist> searchDentistsByName(String name) throws DatabaseException {
        if (name == null || name.trim().isEmpty()) {
            return dentistDAO.findAllActive();
        }
        return dentistDAO.findByName(name.trim());
    }

    @Override
    public List<Dentist> getDentistsBySpecialization(String specialization) throws DatabaseException {
        if (specialization == null || specialization.trim().isEmpty()) {
            return dentistDAO.findAllActive();
        }
        return dentistDAO.findBySpecialization(specialization.trim());
    }

    @Override
    public List<Dentist> getAllDentists() throws DatabaseException {
        return dentistDAO.findAll();
    }

    @Override
    public List<Dentist> getActiveDentists() throws DatabaseException {
        return dentistDAO.findAllActive();
    }

    @Override
    public List<Dentist> getAvailableDentists() throws DatabaseException {
        return dentistDAO.findAvailableDentists();
    }

    @Override
    public Dentist updateDentist(Dentist dentist) throws ValidationException, ResourceNotFoundException, DatabaseException {
        logger.info("Updating dentist: {}", dentist.getDentistNumber());

        // Validate dentist exists
        Optional<Dentist> existing = dentistDAO.findById(dentist.getDentistId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Dentist", String.valueOf(dentist.getDentistId()));
        }

        validateDentist(dentist);

        Dentist updated = dentistDAO.update(dentist);
        logger.info("Dentist updated successfully: {}", updated.getDentistNumber());
        return updated;
    }

    @Override
    public boolean updateDentistAvailability(int dentistId, AvailabilityStatus status)
            throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(dentistId, "Dentist");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        Optional<Dentist> dentist = dentistDAO.findById(dentistId);
        if (dentist.isEmpty()) {
            throw new ResourceNotFoundException("Dentist", String.valueOf(dentistId));
        }

        boolean updated = dentistDAO.updateAvailabilityStatus(dentistId, status);
        logger.info("Dentist availability updated: {} -> {}", dentistId, status);
        return updated;
    }

    @Override
    public boolean deactivateDentist(int dentistId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(dentistId, "Dentist");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        Optional<Dentist> dentist = dentistDAO.findById(dentistId);
        if (dentist.isEmpty()) {
            throw new ResourceNotFoundException("Dentist", String.valueOf(dentistId));
        }

        boolean deactivated = dentistDAO.delete(dentistId);
        logger.info("Dentist deactivated: {}", dentistId);
        return deactivated;
    }

    /**
     * Validate dentist data.
     */
    private void validateDentist(Dentist dentist) throws ValidationException {
        ValidationUtil.validateRequired(dentist, "Dentist");
        ValidationUtil.validateName(dentist.getFullName());

        if (dentist.getContactNumber() != null && !dentist.getContactNumber().trim().isEmpty()) {
            ValidationUtil.validateContactNumber(dentist.getContactNumber());
        }

        if (dentist.getEmail() != null && !dentist.getEmail().trim().isEmpty()) {
            ValidationUtil.validateEmail(dentist.getEmail());
        }

        if (dentist.getConsultationFee() != null &&
                dentist.getConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Consultation fee cannot be negative.");
        }
    }
}