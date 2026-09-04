package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.enums.PaymentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BillDAO interface using JDBC.
 */
public class BillDAOImpl implements BillDAO {

    private static final Logger logger = LogManager.getLogger(BillDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public BillDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Bill save(Bill bill) throws DatabaseException {
        String sql = "INSERT INTO bills (bill_number, appointment_id, patient_id, treatment_cost, " +
                "consultation_fee, subtotal, discount, tax, total_amount, payment_status, " +
                "paid_amount, bill_date, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, bill.getBillNumber());
            stmt.setInt(2, bill.getAppointmentId());
            stmt.setInt(3, bill.getPatientId());
            stmt.setBigDecimal(4, bill.getTreatmentCost());
            stmt.setBigDecimal(5, bill.getConsultationFee());
            stmt.setBigDecimal(6, bill.getSubtotal());
            stmt.setBigDecimal(7, bill.getDiscount());
            stmt.setBigDecimal(8, bill.getTax());
            stmt.setBigDecimal(9, bill.getTotalAmount());
            stmt.setString(10, bill.getPaymentStatus().name());
            stmt.setBigDecimal(11, bill.getPaidAmount());
            stmt.setDate(12, Date.valueOf(bill.getBillDate()));
            stmt.setString(13, bill.getNotes());

            if (bill.getCreatedBy() != null) {
                stmt.setInt(14, bill.getCreatedBy());
            } else {
                stmt.setNull(14, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating bill failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bill.setBillId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating bill failed, no ID obtained.");
                }
            }

            logger.info("Bill saved successfully: {}", bill.getBillNumber());
            return bill;

        } catch (SQLException e) {
            logger.error("Error saving bill: {}", bill.getBillNumber(), e);
            throw new DatabaseException("Failed to save bill", e);
        }
    }

    @Override
    public Optional<Bill> findById(int billId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by ID: {}", billId, e);
            throw new DatabaseException("Failed to find bill by ID", e);
        }
    }

    @Override
    public Optional<Bill> findByBillNumber(String billNumber) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE bill_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, billNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by number: {}", billNumber, e);
            throw new DatabaseException("Failed to find bill by number", e);
        }
    }

    @Override
    public Optional<Bill> findByAppointmentId(int appointmentId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE appointment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by appointment ID: {}", appointmentId, e);
            throw new DatabaseException("Failed to find bill by appointment ID", e);
        }
    }

    @Override
    public List<Bill> findByPatientId(int patientId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE patient_id = ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }

            return bills;

        } catch (SQLException e) {
            logger.error("Error finding bills by patient ID: {}", patientId, e);
            throw new DatabaseException("Failed to find bills by patient ID", e);
        }
    }

    @Override
    public List<Bill> findByPaymentStatus(PaymentStatus status) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE payment_status = ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }

            return bills;

        } catch (SQLException e) {
            logger.error("Error finding bills by payment status: {}", status, e);
            throw new DatabaseException("Failed to find bills by payment status", e);
        }
    }

    @Override
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE bill_date BETWEEN ? AND ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }

            return bills;

        } catch (SQLException e) {
            logger.error("Error finding bills by date range", e);
            throw new DatabaseException("Failed to find bills by date range", e);
        }
    }

    @Override
    public List<Bill> findAll() throws DatabaseException {
        String sql = "SELECT * FROM bills ORDER BY bill_id DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }

            return bills;

        } catch (SQLException e) {
            logger.error("Error finding all bills", e);
            throw new DatabaseException("Failed to find all bills", e);
        }
    }

    @Override
    public Bill update(Bill bill) throws DatabaseException {
        String sql = "UPDATE bills SET treatment_cost = ?, consultation_fee = ?, subtotal = ?, " +
                "discount = ?, tax = ?, total_amount = ?, payment_status = ?, " +
                "paid_amount = ?, notes = ? WHERE bill_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, bill.getTreatmentCost());
            stmt.setBigDecimal(2, bill.getConsultationFee());
            stmt.setBigDecimal(3, bill.getSubtotal());
            stmt.setBigDecimal(4, bill.getDiscount());
            stmt.setBigDecimal(5, bill.getTax());
            stmt.setBigDecimal(6, bill.getTotalAmount());
            stmt.setString(7, bill.getPaymentStatus().name());
            stmt.setBigDecimal(8, bill.getPaidAmount());
            stmt.setString(9, bill.getNotes());
            stmt.setInt(10, bill.getBillId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating bill failed, no rows affected.");
            }

            logger.info("Bill updated successfully: {}", bill.getBillNumber());
            return bill;

        } catch (SQLException e) {
            logger.error("Error updating bill: {}", bill.getBillNumber(), e);
            throw new DatabaseException("Failed to update bill", e);
        }
    }

    @Override
    public boolean updatePaymentStatus(int billId, PaymentStatus status) throws DatabaseException {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, billId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Bill payment status updated: {} -> {}", billId, status);
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error updating bill payment status: {}", billId, e);
            throw new DatabaseException("Failed to update bill payment status", e);
        }
    }

    @Override
    public boolean updatePaidAmount(int billId, BigDecimal paidAmount) throws DatabaseException {
        String sql = "UPDATE bills SET paid_amount = ? WHERE bill_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, paidAmount);
            stmt.setInt(2, billId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Bill paid amount updated: {}", billId);
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error updating bill paid amount: {}", billId, e);
            throw new DatabaseException("Failed to update bill paid amount", e);
        }
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE bill_date BETWEEN ? AND ? " +
                "AND payment_status = 'PAID'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            logger.error("Error calculating total revenue", e);
            throw new DatabaseException("Failed to calculate total revenue", e);
        }
    }

    @Override
    public List<Object[]> getRevenueByTreatment(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT t.treatment_name, COUNT(b.bill_id) as count, SUM(b.total_amount) as revenue " +
                "FROM bills b " +
                "JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE b.bill_date BETWEEN ? AND ? AND b.payment_status = 'PAID' " +
                "GROUP BY t.treatment_id, t.treatment_name " +
                "ORDER BY revenue DESC";

        List<Object[]> results = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            rs.getString("treatment_name"),
                            rs.getInt("count"),
                            rs.getBigDecimal("revenue")
                    });
                }
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting revenue by treatment", e);
            throw new DatabaseException("Failed to get revenue by treatment", e);
        }
    }

    /**
     * Map ResultSet to Bill object.
     */
    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setAppointmentId(rs.getInt("appointment_id"));
        bill.setPatientId(rs.getInt("patient_id"));
        bill.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        bill.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        bill.setSubtotal(rs.getBigDecimal("subtotal"));
        bill.setDiscount(rs.getBigDecimal("discount"));
        bill.setTax(rs.getBigDecimal("tax"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
        bill.setPaidAmount(rs.getBigDecimal("paid_amount"));
        bill.setBillDate(rs.getDate("bill_date").toLocalDate());
        bill.setNotes(rs.getString("notes"));

        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            bill.setCreatedBy(createdBy);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            bill.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            bill.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return bill;
    }
}