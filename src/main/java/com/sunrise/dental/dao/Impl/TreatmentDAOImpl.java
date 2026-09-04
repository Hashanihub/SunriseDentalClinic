package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Treatment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TreatmentDAO interface using JDBC.
 */
public class TreatmentDAOImpl implements TreatmentDAO {

    private static final Logger logger = LogManager.getLogger(TreatmentDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public TreatmentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Treatment save(Treatment treatment) throws DatabaseException {
        String sql = "INSERT INTO treatments (treatment_code, treatment_name, description, " +
                "treatment_cost, consultation_fee, active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, treatment.getTreatmentCode());
            stmt.setString(2, treatment.getTreatmentName());
            stmt.setString(3, treatment.getDescription());
            stmt.setBigDecimal(4, treatment.getTreatmentCost());
            stmt.setBigDecimal(5, treatment.getConsultationFee());
            stmt.setBoolean(6, treatment.isActive());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating treatment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    treatment.setTreatmentId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating treatment failed, no ID obtained.");
                }
            }

            logger.info("Treatment saved successfully: {}", treatment.getTreatmentCode());
            return treatment;

        } catch (SQLException e) {
            logger.error("Error saving treatment: {}", treatment.getTreatmentCode(), e);
            throw new DatabaseException("Failed to save treatment", e);
        }
    }

    @Override
    public Optional<Treatment> findById(int treatmentId) throws DatabaseException {
        String sql = "SELECT * FROM treatments WHERE treatment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, treatmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTreatment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding treatment by ID: {}", treatmentId, e);
            throw new DatabaseException("Failed to find treatment by ID", e);
        }
    }

    @Override
    public Optional<Treatment> findByCode(String treatmentCode) throws DatabaseException {
        String sql = "SELECT * FROM treatments WHERE treatment_code = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, treatmentCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTreatment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding treatment by code: {}", treatmentCode, e);
            throw new DatabaseException("Failed to find treatment by code", e);
        }
    }

    @Override
    public List<Treatment> findByName(String name) throws DatabaseException {
        String sql = "SELECT * FROM treatments WHERE treatment_name LIKE ?";
        List<Treatment> treatments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    treatments.add(mapResultSetToTreatment(rs));
                }
            }

            return treatments;

        } catch (SQLException e) {
            logger.error("Error finding treatments by name: {}", name, e);
            throw new DatabaseException("Failed to find treatments by name", e);
        }
    }

    @Override
    public List<Treatment> findAll() throws DatabaseException {
        String sql = "SELECT * FROM treatments ORDER BY treatment_id";
        List<Treatment> treatments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                treatments.add(mapResultSetToTreatment(rs));
            }

            return treatments;

        } catch (SQLException e) {
            logger.error("Error finding all treatments", e);
            throw new DatabaseException("Failed to find all treatments", e);
        }
    }

    @Override
    public List<Treatment> findAllActive() throws DatabaseException {
        String sql = "SELECT * FROM treatments WHERE active = true ORDER BY treatment_id";
        List<Treatment> treatments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                treatments.add(mapResultSetToTreatment(rs));
            }

            return treatments;

        } catch (SQLException e) {
            logger.error("Error finding all active treatments", e);
            throw new DatabaseException("Failed to find all active treatments", e);
        }
    }

    @Override
    public Treatment update(Treatment treatment) throws DatabaseException {
        String sql = "UPDATE treatments SET treatment_name = ?, description = ?, " +
                "treatment_cost = ?, consultation_fee = ?, active = ? WHERE treatment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, treatment.getTreatmentName());
            stmt.setString(2, treatment.getDescription());
            stmt.setBigDecimal(3, treatment.getTreatmentCost());
            stmt.setBigDecimal(4, treatment.getConsultationFee());
            stmt.setBoolean(5, treatment.isActive());
            stmt.setInt(6, treatment.getTreatmentId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating treatment failed, no rows affected.");
            }

            logger.info("Treatment updated successfully: {}", treatment.getTreatmentCode());
            return treatment;

        } catch (SQLException e) {
            logger.error("Error updating treatment: {}", treatment.getTreatmentCode(), e);
            throw new DatabaseException("Failed to update treatment", e);
        }
    }

    @Override
    public boolean delete(int treatmentId) throws DatabaseException {
        String sql = "UPDATE treatments SET active = false WHERE treatment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, treatmentId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Treatment soft deleted: {}", treatmentId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting treatment: {}", treatmentId, e);
            throw new DatabaseException("Failed to delete treatment", e);
        }
    }

    @Override
    public boolean treatmentCodeExists(String treatmentCode) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM treatments WHERE treatment_code = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, treatmentCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking treatment code existence: {}", treatmentCode, e);
            throw new DatabaseException("Failed to check treatment code existence", e);
        }
    }

    /**
     * Map ResultSet to Treatment object.
     */
    private Treatment mapResultSetToTreatment(ResultSet rs) throws SQLException {
        Treatment treatment = new Treatment();
        treatment.setTreatmentId(rs.getInt("treatment_id"));
        treatment.setTreatmentCode(rs.getString("treatment_code"));
        treatment.setTreatmentName(rs.getString("treatment_name"));
        treatment.setDescription(rs.getString("description"));
        treatment.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        treatment.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        treatment.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            treatment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            treatment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return treatment;
    }
}