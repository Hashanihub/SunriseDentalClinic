package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.enums.AppointmentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AppointmentDAO interface using JDBC.
 */
public class AppointmentDAOImpl implements AppointmentDAO {

    private static final Logger logger = LogManager.getLogger(AppointmentDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public AppointmentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Appointment save(Appointment appointment) throws DatabaseException {
        if (!isDentistAvailable(appointment.getDentistId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime().toString())) {
            throw new DatabaseException("Dentist is not available at this time");
        }

        String sql = "INSERT INTO appointments (appointment_number, patient_id, dentist_id, " +
                "treatment_id, appointment_date, appointment_time, status, notes, " +
                "cancellation_reason, rescheduled_from, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setNull(1, Types.VARCHAR);
            stmt.setInt(2, appointment.getPatientId());
            stmt.setInt(3, appointment.getDentistId());
            stmt.setInt(4, appointment.getTreatmentId());
            stmt.setDate(5, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(6, Time.valueOf(appointment.getAppointmentTime()));
            stmt.setString(7, appointment.getStatus().name());
            stmt.setString(8, appointment.getNotes());
            stmt.setString(9, appointment.getCancellationReason());

            if (appointment.getRescheduledFrom() != null) {
                stmt.setInt(10, appointment.getRescheduledFrom());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            if (appointment.getCreatedBy() != null) {
                stmt.setInt(11, appointment.getCreatedBy());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating appointment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getInt(1));
                }
            }

            Appointment saved = findById(appointment.getAppointmentId())
                    .orElseThrow(() -> new DatabaseException("Failed to retrieve saved appointment"));

            logger.info("Appointment saved successfully: {}", saved.getAppointmentNumber());
            return saved;

        } catch (SQLException e) {
            logger.error("Error saving appointment", e);
            throw new DatabaseException("Failed to save appointment", e);
        }
    }

    @Override
    public Optional<Appointment> findById(int appointmentId) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding appointment by ID: {}", appointmentId, e);
            throw new DatabaseException("Failed to find appointment by ID", e);
        }
    }

    @Override
    public Optional<Appointment> findByAppointmentNumber(String appointmentNumber) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE appointment_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointmentNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding appointment by number: {}", appointmentNumber, e);
            throw new DatabaseException("Failed to find appointment by number", e);
        }
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by patient ID: {}", patientId, e);
            throw new DatabaseException("Failed to find appointments by patient ID", e);
        }
    }

    @Override
    public List<Appointment> findByDentistId(int dentistId) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE dentist_id = ? ORDER BY appointment_date DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by dentist ID: {}", dentistId, e);
            throw new DatabaseException("Failed to find appointments by dentist ID", e);
        }
    }

    @Override
    public List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE dentist_id = ? AND appointment_date = ? " +
                "ORDER BY appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by dentist and date", e);
            throw new DatabaseException("Failed to find appointments by dentist and date", e);
        }
    }

    // ✅ FIXED: findByDate - Inline mapping to avoid ResultSet closed error
    @Override
    public List<Appointment> findByDate(LocalDate date) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE appointment_date = ? ORDER BY appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("appointment_id"));
                    appointment.setAppointmentNumber(rs.getString("appointment_number"));
                    appointment.setPatientId(rs.getInt("patient_id"));
                    appointment.setDentistId(rs.getInt("dentist_id"));
                    appointment.setTreatmentId(rs.getInt("treatment_id"));
                    appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                    appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
                    appointment.setStatus(AppointmentStatus.fromString(rs.getString("status")));
                    appointment.setNotes(rs.getString("notes"));
                    appointment.setCancellationReason(rs.getString("cancellation_reason"));

                    int rescheduledFrom = rs.getInt("rescheduled_from");
                    if (!rs.wasNull()) {
                        appointment.setRescheduledFrom(rescheduledFrom);
                    }

                    int createdBy = rs.getInt("created_by");
                    if (!rs.wasNull()) {
                        appointment.setCreatedBy(createdBy);
                    }

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        appointment.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        appointment.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    appointments.add(appointment);
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by date: {}", date, e);
            throw new DatabaseException("Failed to find appointments by date", e);
        }
    }

    @Override
    public List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE appointment_date BETWEEN ? AND ? " +
                "ORDER BY appointment_date, appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by date range", e);
            throw new DatabaseException("Failed to find appointments by date range", e);
        }
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) throws DatabaseException {
        String sql = "SELECT * FROM appointments WHERE status = ? ORDER BY appointment_date";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by status: {}", status, e);
            throw new DatabaseException("Failed to find appointments by status", e);
        }
    }

    @Override
    public List<Appointment> findTodayAppointments() throws DatabaseException {
        return findByDate(LocalDate.now());
    }

    @Override
    public Appointment update(Appointment appointment) throws DatabaseException {
        String sql = "UPDATE appointments SET patient_id = ?, dentist_id = ?, treatment_id = ?, " +
                "appointment_date = ?, appointment_time = ?, status = ?, notes = ?, " +
                "cancellation_reason = ?, rescheduled_from = ? WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDentistId());
            stmt.setInt(3, appointment.getTreatmentId());
            stmt.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            stmt.setString(6, appointment.getStatus().name());
            stmt.setString(7, appointment.getNotes());
            stmt.setString(8, appointment.getCancellationReason());

            if (appointment.getRescheduledFrom() != null) {
                stmt.setInt(9, appointment.getRescheduledFrom());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setInt(10, appointment.getAppointmentId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating appointment failed, no rows affected.");
            }

            logger.info("Appointment updated successfully: {}", appointment.getAppointmentNumber());
            return appointment;

        } catch (SQLException e) {
            logger.error("Error updating appointment: {}", appointment.getAppointmentNumber(), e);
            throw new DatabaseException("Failed to update appointment", e);
        }
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus status) throws DatabaseException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, appointmentId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error updating appointment status: {}", appointmentId, e);
            throw new DatabaseException("Failed to update appointment status", e);
        }
    }

    @Override
    public boolean cancelAppointment(int appointmentId, String reason) throws DatabaseException {
        String sql = "UPDATE appointments SET status = 'CANCELLED', cancellation_reason = ? " +
                "WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reason);
            stmt.setInt(2, appointmentId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Error cancelling appointment: {}", appointmentId, e);
            throw new DatabaseException("Failed to cancel appointment", e);
        }
    }

    @Override
    public boolean delete(int appointmentId) throws DatabaseException {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("Error deleting appointment: {}", appointmentId, e);
            throw new DatabaseException("Failed to delete appointment", e);
        }
    }

    @Override
    public boolean isDentistAvailable(int dentistId, LocalDate date, String time) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = ? " +
                "AND appointment_time = ? AND status NOT IN ('CANCELLED', 'NO_SHOW')";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(LocalTime.parse(time)));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

            return true;

        } catch (SQLException e) {
            logger.error("Error checking dentist availability", e);
            throw new DatabaseException("Failed to check dentist availability", e);
        }
    }

    @Override
    public long countByStatus(AppointmentStatus status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments";
        if (status != null) {
            sql += " WHERE status = '" + status.name() + "'";
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting appointments by status", e);
            throw new DatabaseException("Failed to count appointments by status", e);
        }
    }

    @Override
    public long countTodayAppointments() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE() " +
                "AND status NOT IN ('CANCELLED', 'NO_SHOW')";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error counting today's appointments", e);
            throw new DatabaseException("Failed to count today's appointments", e);
        }
    }

    /**
     * Map ResultSet to Appointment object.
     */
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setAppointmentNumber(rs.getString("appointment_number"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDentistId(rs.getInt("dentist_id"));
        appointment.setTreatmentId(rs.getInt("treatment_id"));
        appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        appointment.setStatus(AppointmentStatus.fromString(rs.getString("status")));
        appointment.setNotes(rs.getString("notes"));
        appointment.setCancellationReason(rs.getString("cancellation_reason"));

        int rescheduledFrom = rs.getInt("rescheduled_from");
        if (!rs.wasNull()) {
            appointment.setRescheduledFrom(rescheduledFrom);
        }

        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            appointment.setCreatedBy(createdBy);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            appointment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            appointment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return appointment;
    }
}