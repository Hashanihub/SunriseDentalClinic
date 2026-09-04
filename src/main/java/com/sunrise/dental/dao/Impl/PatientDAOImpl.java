package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.enums.Gender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PatientDAO interface using JDBC.
 */
public class PatientDAOImpl implements PatientDAO {

    private static final Logger logger = LogManager.getLogger(PatientDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public PatientDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Patient save(Patient patient) throws DatabaseException {
        String sql = "INSERT INTO patients (patient_number, full_name, address, contact_number, " +
                "email, date_of_birth, gender, registration_date, active, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getPatientNumber());
            stmt.setString(2, patient.getFullName());
            stmt.setString(3, patient.getAddress());
            stmt.setString(4, patient.getContactNumber());
            stmt.setString(5, patient.getEmail());
            stmt.setDate(6, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null);
            stmt.setString(7, patient.getGender() != null ? patient.getGender().name() : null);
            stmt.setDate(8, Date.valueOf(patient.getRegistrationDate()));
            stmt.setBoolean(9, patient.isActive());
            stmt.setString(10, patient.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating patient failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setPatientId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating patient failed, no ID obtained.");
                }
            }

            logger.info("Patient saved successfully: {}", patient.getPatientNumber());
            return patient;

        } catch (SQLException e) {
            logger.error("Error saving patient: {}", patient.getPatientNumber(), e);
            throw new DatabaseException("Failed to save patient", e);
        }
    }

    @Override
    public Optional<Patient> findById(int patientId) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by ID: {}", patientId, e);
            throw new DatabaseException("Failed to find patient by ID", e);
        }
    }

    @Override
    public Optional<Patient> findByPatientNumber(String patientNumber) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE patient_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by number: {}", patientNumber, e);
            throw new DatabaseException("Failed to find patient by number", e);
        }
    }

    @Override
    public List<Patient> findByName(String name) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE full_name LIKE ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding patients by name: {}", name, e);
            throw new DatabaseException("Failed to find patients by name", e);
        }
    }

    @Override
    public Optional<Patient> findByContactNumber(String contactNumber) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE contact_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contactNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by contact: {}", contactNumber, e);
            throw new DatabaseException("Failed to find patient by contact", e);
        }
    }

    @Override
    public List<Patient> findAll() throws DatabaseException {
        String sql = "SELECT * FROM patients ORDER BY patient_id";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding all patients", e);
            throw new DatabaseException("Failed to find all patients", e);
        }
    }

    @Override
    public List<Patient> findAllActive() throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE active = true ORDER BY patient_id";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding all active patients", e);
            throw new DatabaseException("Failed to find all active patients", e);
        }
    }

    @Override
    public Patient update(Patient patient) throws DatabaseException {
        String sql = "UPDATE patients SET full_name = ?, address = ?, contact_number = ?, " +
                "email = ?, date_of_birth = ?, gender = ?, active = ?, notes = ? " +
                "WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFullName());
            stmt.setString(2, patient.getAddress());
            stmt.setString(3, patient.getContactNumber());
            stmt.setString(4, patient.getEmail());
            stmt.setDate(5, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null);
            stmt.setString(6, patient.getGender() != null ? patient.getGender().name() : null);
            stmt.setBoolean(7, patient.isActive());
            stmt.setString(8, patient.getNotes());
            stmt.setInt(9, patient.getPatientId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating patient failed, no rows affected.");
            }

            logger.info("Patient updated successfully: {}", patient.getPatientNumber());
            return patient;

        } catch (SQLException e) {
            logger.error("Error updating patient: {}", patient.getPatientNumber(), e);
            throw new DatabaseException("Failed to update patient", e);
        }
    }

    @Override
    public boolean delete(int patientId) throws DatabaseException {
        String sql = "UPDATE patients SET active = false WHERE patient_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Patient soft deleted: {}", patientId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting patient: {}", patientId, e);
            throw new DatabaseException("Failed to delete patient", e);
        }
    }

    @Override
    public boolean patientNumberExists(String patientNumber) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM patients WHERE patient_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking patient number existence: {}", patientNumber, e);
            throw new DatabaseException("Failed to check patient number existence", e);
        }
    }

    @Override
    public boolean contactNumberExists(String contactNumber, int excludePatientId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM patients WHERE contact_number = ? AND patient_id != ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contactNumber);
            stmt.setInt(2, excludePatientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking contact number existence: {}", contactNumber, e);
            throw new DatabaseException("Failed to check contact number existence", e);
        }
    }

    @Override
    public long count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM patients";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error counting patients", e);
            throw new DatabaseException("Failed to count patients", e);
        }
    }

    @Override
    public List<Patient> findByRegistrationDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE registration_date BETWEEN ? AND ?";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding patients by registration date range", e);
            throw new DatabaseException("Failed to find patients by registration date range", e);
        }
    }

    /**
     * Map ResultSet to Patient object.
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setPatientNumber(rs.getString("patient_number"));
        patient.setFullName(rs.getString("full_name"));
        patient.setAddress(rs.getString("address"));
        patient.setContactNumber(rs.getString("contact_number"));
        patient.setEmail(rs.getString("email"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }

        String gender = rs.getString("gender");
        if (gender != null) {
            patient.setGender(Gender.fromString(gender));
        }

        patient.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        patient.setActive(rs.getBoolean("active"));
        patient.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return patient;
    }
}