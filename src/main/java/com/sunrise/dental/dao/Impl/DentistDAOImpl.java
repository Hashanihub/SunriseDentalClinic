package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.DentistDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.enums.AvailabilityStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DentistDAO interface using JDBC.
 */
public class DentistDAOImpl implements DentistDAO {

    private static final Logger logger = LogManager.getLogger(DentistDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public DentistDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Dentist save(Dentist dentist) throws DatabaseException {
        String sql = "INSERT INTO dentists (dentist_number, full_name, specialization, contact_number, " +
                "email, consultation_fee, availability_status, active, joined_date, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, dentist.getDentistNumber());
            stmt.setString(2, dentist.getFullName());
            stmt.setString(3, dentist.getSpecialization());
            stmt.setString(4, dentist.getContactNumber());
            stmt.setString(5, dentist.getEmail());
            stmt.setBigDecimal(6, dentist.getConsultationFee());
            stmt.setString(7, dentist.getAvailabilityStatus().name());
            stmt.setBoolean(8, dentist.isActive());
            stmt.setDate(9, dentist.getJoinedDate() != null ? Date.valueOf(dentist.getJoinedDate()) : null);
            stmt.setString(10, dentist.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating dentist failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dentist.setDentistId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating dentist failed, no ID obtained.");
                }
            }

            logger.info("Dentist saved successfully: {}", dentist.getDentistNumber());
            return dentist;

        } catch (SQLException e) {
            logger.error("Error saving dentist: {}", dentist.getDentistNumber(), e);
            throw new DatabaseException("Failed to save dentist", e);
        }
    }

    @Override
    public Optional<Dentist> findById(int dentistId) throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE dentist_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDentist(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding dentist by ID: {}", dentistId, e);
            throw new DatabaseException("Failed to find dentist by ID", e);
        }
    }

    @Override
    public Optional<Dentist> findByDentistNumber(String dentistNumber) throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE dentist_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dentistNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDentist(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding dentist by number: {}", dentistNumber, e);
            throw new DatabaseException("Failed to find dentist by number", e);
        }
    }

    @Override
    public List<Dentist> findByName(String name) throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE full_name LIKE ?";
        List<Dentist> dentists = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dentists.add(mapResultSetToDentist(rs));
                }
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding dentists by name: {}", name, e);
            throw new DatabaseException("Failed to find dentists by name", e);
        }
    }

    @Override
    public List<Dentist> findBySpecialization(String specialization) throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE specialization LIKE ?";
        List<Dentist> dentists = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + specialization + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dentists.add(mapResultSetToDentist(rs));
                }
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding dentists by specialization: {}", specialization, e);
            throw new DatabaseException("Failed to find dentists by specialization", e);
        }
    }

    @Override
    public List<Dentist> findAvailableDentists() throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE availability_status = 'AVAILABLE' AND active = true";
        List<Dentist> dentists = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dentists.add(mapResultSetToDentist(rs));
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding available dentists", e);
            throw new DatabaseException("Failed to find available dentists", e);
        }
    }

    @Override
    public List<Dentist> findAll() throws DatabaseException {
        String sql = "SELECT * FROM dentists ORDER BY dentist_id";
        List<Dentist> dentists = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dentists.add(mapResultSetToDentist(rs));
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding all dentists", e);
            throw new DatabaseException("Failed to find all dentists", e);
        }
    }

    @Override
    public List<Dentist> findAllActive() throws DatabaseException {
        String sql = "SELECT * FROM dentists WHERE active = true ORDER BY dentist_id";
        List<Dentist> dentists = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dentists.add(mapResultSetToDentist(rs));
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding all active dentists", e);
            throw new DatabaseException("Failed to find all active dentists", e);
        }
    }

    @Override
    public Dentist update(Dentist dentist) throws DatabaseException {
        String sql = "UPDATE dentists SET full_name = ?, specialization = ?, contact_number = ?, " +
                "email = ?, consultation_fee = ?, availability_status = ?, active = ?, " +
                "notes = ? WHERE dentist_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dentist.getFullName());
            stmt.setString(2, dentist.getSpecialization());
            stmt.setString(3, dentist.getContactNumber());
            stmt.setString(4, dentist.getEmail());
            stmt.setBigDecimal(5, dentist.getConsultationFee());
            stmt.setString(6, dentist.getAvailabilityStatus().name());
            stmt.setBoolean(7, dentist.isActive());
            stmt.setString(8, dentist.getNotes());
            stmt.setInt(9, dentist.getDentistId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating dentist failed, no rows affected.");
            }

            logger.info("Dentist updated successfully: {}", dentist.getDentistNumber());
            return dentist;

        } catch (SQLException e) {
            logger.error("Error updating dentist: {}", dentist.getDentistNumber(), e);
            throw new DatabaseException("Failed to update dentist", e);
        }
    }

    @Override
    public boolean updateAvailabilityStatus(int dentistId, AvailabilityStatus status) throws DatabaseException {
        String sql = "UPDATE dentists SET availability_status = ? WHERE dentist_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, dentistId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Dentist availability updated: {} -> {}", dentistId, status);
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error updating dentist availability: {}", dentistId, e);
            throw new DatabaseException("Failed to update dentist availability", e);
        }
    }

    @Override
    public boolean delete(int dentistId) throws DatabaseException {
        String sql = "UPDATE dentists SET active = false WHERE dentist_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Dentist soft deleted: {}", dentistId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting dentist: {}", dentistId, e);
            throw new DatabaseException("Failed to delete dentist", e);
        }
    }

    /**
     * Map ResultSet to Dentist object.
     */
    private Dentist mapResultSetToDentist(ResultSet rs) throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setDentistId(rs.getInt("dentist_id"));
        dentist.setDentistNumber(rs.getString("dentist_number"));
        dentist.setFullName(rs.getString("full_name"));
        dentist.setSpecialization(rs.getString("specialization"));
        dentist.setContactNumber(rs.getString("contact_number"));
        dentist.setEmail(rs.getString("email"));
        dentist.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        dentist.setAvailabilityStatus(AvailabilityStatus.fromString(rs.getString("availability_status")));
        dentist.setActive(rs.getBoolean("active"));

        Date joinedDate = rs.getDate("joined_date");
        if (joinedDate != null) {
            dentist.setJoinedDate(joinedDate.toLocalDate());
        }

        dentist.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dentist.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            dentist.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return dentist;
    }
}