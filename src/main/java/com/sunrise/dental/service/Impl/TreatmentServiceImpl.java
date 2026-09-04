package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.dao.Impl.TreatmentDAOImpl;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.service.TreatmentService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TreatmentService interface.
 */
public class TreatmentServiceImpl implements TreatmentService {

    private static final Logger logger = LogManager.getLogger(TreatmentServiceImpl.class);
    private final TreatmentDAO treatmentDAO;

    public TreatmentServiceImpl() {
        this.treatmentDAO = new TreatmentDAOImpl();
    }

    @Override
    public Treatment addTreatment(Treatment treatment) throws ValidationException, DatabaseException {
        logger.info("Adding new treatment: {}", treatment.getTreatmentName());

        validateTreatment(treatment);

        // Check if treatment code already exists
        if (treatmentDAO.treatmentCodeExists(treatment.getTreatmentCode())) {
            throw new ValidationException("Treatment code already exists. Please use a different code.");
        }

        Treatment saved = treatmentDAO.save(treatment);
        logger.info("Treatment added successfully: {}", saved.getTreatmentCode());
        return saved;
    }

    @Override
    public Treatment getTreatmentById(int treatmentId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(treatmentId, "Treatment");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Treatment> treatment = treatmentDAO.findById(treatmentId);
        if (treatment.isEmpty()) {
            throw new ResourceNotFoundException("Treatment", String.valueOf(treatmentId));
        }
        return treatment.get();
    }

    @Override
    public Treatment getTreatmentByCode(String treatmentCode) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateRequired(treatmentCode, "Treatment code");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Treatment> treatment = treatmentDAO.findByCode(treatmentCode);
        if (treatment.isEmpty()) {
            throw new ResourceNotFoundException("Treatment", treatmentCode);
        }
        return treatment.get();
    }

    @Override
    public List<Treatment> searchTreatmentsByName(String name) throws DatabaseException {
        if (name == null || name.trim().isEmpty()) {
            return treatmentDAO.findAllActive();
        }
        return treatmentDAO.findByName(name.trim());
    }

    @Override
    public List<Treatment> getAllTreatments() throws DatabaseException {
        return treatmentDAO.findAll();
    }

    @Override
    public List<Treatment> getActiveTreatments() throws DatabaseException {
        return treatmentDAO.findAllActive();
    }

    @Override
    public Treatment updateTreatment(Treatment treatment) throws ValidationException, ResourceNotFoundException, DatabaseException {
        logger.info("Updating treatment: {}", treatment.getTreatmentCode());

        // Validate treatment exists
        Optional<Treatment> existing = treatmentDAO.findById(treatment.getTreatmentId());
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Treatment", String.valueOf(treatment.getTreatmentId()));
        }

        validateTreatment(treatment);

        Treatment updated = treatmentDAO.update(treatment);
        logger.info("Treatment updated successfully: {}", updated.getTreatmentCode());
        return updated;
    }

    @Override
    public boolean deactivateTreatment(int treatmentId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(treatmentId, "Treatment");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        Optional<Treatment> treatment = treatmentDAO.findById(treatmentId);
        if (treatment.isEmpty()) {
            throw new ResourceNotFoundException("Treatment", String.valueOf(treatmentId));
        }

        boolean deactivated = treatmentDAO.delete(treatmentId);
        logger.info("Treatment deactivated: {}", treatmentId);
        return deactivated;
    }

    @Override
    public BigDecimal calculateTotalCost(int treatmentId) throws ResourceNotFoundException, DatabaseException {
        Treatment treatment = getTreatmentById(treatmentId);
        return treatment.getTotalCost();
    }

    /**
     * Validate treatment data.
     */
    private void validateTreatment(Treatment treatment) throws ValidationException {
        ValidationUtil.validateRequired(treatment, "Treatment");
        ValidationUtil.validateRequired(treatment.getTreatmentName(), "Treatment name");
        ValidationUtil.validateRequired(treatment.getTreatmentCode(), "Treatment code");

        if (treatment.getTreatmentCost() == null || treatment.getTreatmentCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Treatment cost cannot be negative.");
        }

        if (treatment.getConsultationFee() == null || treatment.getConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Consultation fee cannot be negative.");
        }
    }
}