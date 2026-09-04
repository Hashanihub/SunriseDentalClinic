package com.sunrise.dental.service;

import com.sunrise.dental.dto.BillRequestDTO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Billing service interface.
 */
public interface BillingService {

    /**
     * Generate a bill for an appointment.
     * @param request The bill request
     * @return The generated bill
     * @throws ValidationException if validation fails
     * @throws ResourceNotFoundException if appointment not found
     * @throws DatabaseException if database error occurs
     */
    Bill generateBill(BillRequestDTO request) throws ValidationException, ResourceNotFoundException, DatabaseException;

    /**
     * Get bill by ID.
     * @param billId The bill ID
     * @return The bill
     * @throws ResourceNotFoundException if bill not found
     * @throws DatabaseException if database error occurs
     */
    Bill getBillById(int billId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get bill by bill number.
     * @param billNumber The bill number
     * @return The bill
     * @throws ResourceNotFoundException if bill not found
     * @throws DatabaseException if database error occurs
     */
    Bill getBillByNumber(String billNumber) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get bill by appointment ID.
     * @param appointmentId The appointment ID
     * @return The bill
     * @throws ResourceNotFoundException if bill not found
     * @throws DatabaseException if database error occurs
     */
    Bill getBillByAppointmentId(int appointmentId) throws ResourceNotFoundException, DatabaseException;

    /**
     * Get bills by patient ID.
     * @param patientId The patient ID
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> getBillsByPatientId(int patientId) throws DatabaseException;

    /**
     * Get bills by payment status.
     * @param status The payment status
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> getBillsByPaymentStatus(PaymentStatus status) throws DatabaseException;

    /**
     * Get bills by date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of bills
     * @throws DatabaseException if database error occurs
     */
    List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Process payment for a bill.
     * @param billId The bill ID
     * @param payment The payment
     * @return Updated bill
     * @throws ResourceNotFoundException if bill not found
     * @throws ValidationException if validation fails
     * @throws DatabaseException if database error occurs
     */
    Bill processPayment(int billId, Payment payment)
            throws ResourceNotFoundException, ValidationException, DatabaseException;

    /**
     * Get total revenue for date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue
     * @throws DatabaseException if database error occurs
     */
    BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Get revenue by treatment for date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of revenue data
     * @throws DatabaseException if database error occurs
     */
    List<RevenueData> getRevenueByTreatment(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Inner class for revenue data.
     */
    class RevenueData {
        private String treatmentName;
        private int count;
        private BigDecimal revenue;

        public RevenueData(String treatmentName, int count, BigDecimal revenue) {
            this.treatmentName = treatmentName;
            this.count = count;
            this.revenue = revenue;
        }

        public String getTreatmentName() { return treatmentName; }
        public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }
}