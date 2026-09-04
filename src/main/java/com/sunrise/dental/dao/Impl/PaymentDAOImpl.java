package com.sunrise.dental.dao.Impl;

import com.sunrise.dental.config.DatabaseConnection;
import com.sunrise.dental.dao.PaymentDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.enums.PaymentMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PaymentDAO interface using JDBC.
 */
public class PaymentDAOImpl implements PaymentDAO {

    private static final Logger logger = LogManager.getLogger(PaymentDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public PaymentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Payment save(Payment payment) throws DatabaseException {
        String sql = "INSERT INTO payments (payment_number, bill_id, amount, payment_date, " +
                "payment_method, reference_number, notes, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, payment.getPaymentNumber());
            stmt.setInt(2, payment.getBillId());
            stmt.setBigDecimal(3, payment.getAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(5, payment.getPaymentMethod().name());
            stmt.setString(6, payment.getReferenceNumber());
            stmt.setString(7, payment.getNotes());

            if (payment.getCreatedBy() != null) {
                stmt.setInt(8, payment.getCreatedBy());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating payment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setPaymentId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating payment failed, no ID obtained.");
                }
            }

            logger.info("Payment saved successfully: {}", payment.getPaymentNumber());
            return payment;

        } catch (SQLException e) {
            logger.error("Error saving payment: {}", payment.getPaymentNumber(), e);
            throw new DatabaseException("Failed to save payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(int paymentId) throws DatabaseException {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding payment by ID: {}", paymentId, e);
            throw new DatabaseException("Failed to find payment by ID", e);
        }
    }

    @Override
    public Optional<Payment> findByPaymentNumber(String paymentNumber) throws DatabaseException {
        String sql = "SELECT * FROM payments WHERE payment_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding payment by number: {}", paymentNumber, e);
            throw new DatabaseException("Failed to find payment by number", e);
        }
    }

    @Override
    public List<Payment> findByBillId(int billId) throws DatabaseException {
        String sql = "SELECT * FROM payments WHERE bill_id = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }

            return payments;

        } catch (SQLException e) {
            logger.error("Error finding payments by bill ID: {}", billId, e);
            throw new DatabaseException("Failed to find payments by bill ID", e);
        }
    }

    @Override
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException {
        String sql = "SELECT * FROM payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }

            return payments;

        } catch (SQLException e) {
            logger.error("Error finding payments by date range", e);
            throw new DatabaseException("Failed to find payments by date range", e);
        }
    }

    @Override
    public List<Payment> findByMethod(PaymentMethod method) throws DatabaseException {
        String sql = "SELECT * FROM payments WHERE payment_method = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, method.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }

            return payments;

        } catch (SQLException e) {
            logger.error("Error finding payments by method: {}", method, e);
            throw new DatabaseException("Failed to find payments by method", e);
        }
    }

    @Override
    public List<Payment> findAll() throws DatabaseException {
        String sql = "SELECT * FROM payments ORDER BY payment_id DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }

            return payments;

        } catch (SQLException e) {
            logger.error("Error finding all payments", e);
            throw new DatabaseException("Failed to find all payments", e);
        }
    }

    @Override
    public Payment update(Payment payment) throws DatabaseException {
        String sql = "UPDATE payments SET amount = ?, payment_date = ?, payment_method = ?, " +
                "reference_number = ?, notes = ? WHERE payment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, payment.getAmount());
            stmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(3, payment.getPaymentMethod().name());
            stmt.setString(4, payment.getReferenceNumber());
            stmt.setString(5, payment.getNotes());
            stmt.setInt(6, payment.getPaymentId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating payment failed, no rows affected.");
            }

            logger.info("Payment updated successfully: {}", payment.getPaymentNumber());
            return payment;

        } catch (SQLException e) {
            logger.error("Error updating payment: {}", payment.getPaymentNumber(), e);
            throw new DatabaseException("Failed to update payment", e);
        }
    }

    @Override
    public boolean delete(int paymentId) throws DatabaseException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);
            int affectedRows = stmt.executeUpdate();

            logger.info("Payment deleted: {}", paymentId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error deleting payment: {}", paymentId, e);
            throw new DatabaseException("Failed to delete payment", e);
        }
    }

    @Override
    public BigDecimal getTotalPaidByBill(int billId) throws DatabaseException {
        String sql = "SELECT SUM(amount) FROM payments WHERE bill_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            logger.error("Error getting total paid by bill: {}", billId, e);
            throw new DatabaseException("Failed to get total paid by bill", e);
        }
    }

    @Override
    public BigDecimal getTotalPayments(LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException {
        String sql = "SELECT SUM(amount) FROM payments WHERE payment_date BETWEEN ? AND ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            logger.error("Error getting total payments", e);
            throw new DatabaseException("Failed to get total payments", e);
        }
    }

    @Override
    public List<Object[]> getPaymentSummaryByMethod(LocalDateTime startDate, LocalDateTime endDate)
            throws DatabaseException {
        String sql = "SELECT payment_method, COUNT(*) as count, SUM(amount) as total " +
                "FROM payments WHERE payment_date BETWEEN ? AND ? " +
                "GROUP BY payment_method ORDER BY total DESC";

        List<Object[]> results = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            PaymentMethod.fromString(rs.getString("payment_method")),
                            rs.getInt("count"),
                            rs.getBigDecimal("total")
                    });
                }
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting payment summary by method", e);
            throw new DatabaseException("Failed to get payment summary by method", e);
        }
    }

    /**
     * Map ResultSet to Payment object.
     */
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setPaymentNumber(rs.getString("payment_number"));
        payment.setBillId(rs.getInt("bill_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        payment.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setNotes(rs.getString("notes"));

        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            payment.setCreatedBy(createdBy);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            payment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            payment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return payment;
    }
}