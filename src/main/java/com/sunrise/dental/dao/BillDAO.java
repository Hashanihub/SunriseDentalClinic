package com.sunrise.dental.dao;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Bill entity.
 */
public interface BillDAO {

    /**
     * Save a new bill.
     * @param bill The bill to save
     * @return The saved bill with generated ID
     * @throws DatabaseException if database error occurs
     */
    Bill save(Bill bill) throws DatabaseException;

    /**
     * Find bill by ID.
     * @param billId The bill ID
     * @return Optional containing bill if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Bill> findById(int billId) throws DatabaseException;

    /**
     * Find bill by bill number.
     * @param billNumber The bill number
     * @return Optional containing bill if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Bill> findByBillNumber(String billNumber) throws DatabaseException;

    /**
     * Find bill by appointment ID.
     * @param appointmentId The appointment ID
     * @return Optional containing bill if found
     * @throws DatabaseException if database error occurs
     */
    Optional<Bill> findByAppointmentId(int appointmentId) throws DatabaseException;

    /**
     * Find bills by patient ID.
     * @param patientId The patient ID
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> findByPatientId(int patientId) throws DatabaseException;

    /**
     * Find bills by payment status.
     * @param status The payment status
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> findByPaymentStatus(PaymentStatus status) throws DatabaseException;

    /**
     * Find bills by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Get all bills.
     * @return List of all bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> findAll() throws DatabaseException;

    /**
     * Update bill.
     * @param bill The bill to update
     * @return Updated bill
     * @throws DatabaseException if database error occurs
     */
    Bill update(Bill bill) throws DatabaseException;

    /**
     * Update payment status.
     * @param billId The bill ID
     * @param status The new payment status
     * @return true if updated successfully
     * @throws DatabaseException if database error occurs
     */
    boolean updatePaymentStatus(int billId, PaymentStatus status) throws DatabaseException;

    /**
     * Update paid amount.
     * @param billId The bill ID
     * @param paidAmount The new paid amount
     * @return true if updated successfully
     * @throws DatabaseException if database error occurs
     */
    boolean updatePaidAmount(int billId, java.math.BigDecimal paidAmount) throws DatabaseException;

    /**
     * Get total revenue for date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue
     * @throws DatabaseException if database error occurs
     */
    java.math.BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Get revenue by treatment.
     * @param startDate Start date
     * @param endDate End date
     * @return Map of treatment to revenue
     * @throws DatabaseException if database error occurs
     */
    List<Object[]> getRevenueByTreatment(LocalDate startDate, LocalDate endDate) throws DatabaseException;
}