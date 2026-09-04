package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Payment entity.
 */
public interface PaymentDAO {

    /**
     * Save a new payment.
     * @param payment The payment to save
     * @return The saved payment with generated ID
     * @throws DatabaseException if database error occurs
     */
    Payment save(Payment payment) throws DatabaseException;

    /**
     * Find payment by ID.
     * @param paymentId The payment ID
     * @return Optional containing payment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Payment> findById(int paymentId) throws DatabaseException;

    /**
     * Find payment by payment number.
     * @param paymentNumber The payment number
     * @return Optional containing payment if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Payment> findByPaymentNumber(String paymentNumber) throws DatabaseException;

    /**
     * Find payments by bill ID.
     * @param billId The bill ID
     * @return List of payments
     * @throws DatabaseException if database error occurs
     */
    List<Payment> findByBillId(int billId) throws DatabaseException;

    /**
     * Find payments by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of payments
     * @throws DatabaseException if database error occurs
     */
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException;

    /**
     * Find payments by method.
     * @param method The payment method
     * @return List of payments
     * @throws DatabaseException if database error occurs
     */
    List<Payment> findByMethod(PaymentMethod method) throws DatabaseException;

    /**
     * Get all payments.
     * @return List of all payments
     * @throws DatabaseException if database error occurs
     */
    List<Payment> findAll() throws DatabaseException;

    /**
     * Update payment.
     * @param payment The payment to update
     * @return Updated payment
     * @throws DatabaseException if database error occurs
     */
    Payment update(Payment payment) throws DatabaseException;

    /**
     * Delete payment by ID.
     * @param paymentId The payment ID
     * @return true if deleted successfully
     * @throws DatabaseException if database error occurs
     */
    boolean delete(int paymentId) throws DatabaseException;

    /**
     * Get total payments by bill ID.
     * @param billId The bill ID
     * @return Total amount paid
     * @throws DatabaseException if database error occurs
     */
    java.math.BigDecimal getTotalPaidByBill(int billId) throws DatabaseException;

    /**
     * Get total payments by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Total amount
     * @throws DatabaseException if database error occurs
     */
    java.math.BigDecimal getTotalPayments(LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException;

    /**
     * Get payment summary by method.
     * @param startDate Start date
     * @param endDate End date
     * @return List of payment summaries
     * @throws DatabaseException if database error occurs
     */
    List<Object[]> getPaymentSummaryByMethod(LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException;
}