package com.sunrise.dental.service;

import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Patient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report service interface for generating reports.
 */
public interface ReportService {

    /**
     * Get daily appointment report.
     * @param date The date
     * @return List of appointments for the date
     * @throws DatabaseException if database error occurs
     */
    List<Appointment> getDailyAppointmentReport(LocalDate date) throws DatabaseException;

    /**
     * Get dentist appointment report.
     * @param dentistId The dentist ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of appointments
     * @throws DatabaseException if database error occurs
     * @throws ValidationException if validation fails
     */
    List<Appointment> getDentistAppointmentReport(int dentistId, LocalDate startDate, LocalDate endDate)
            throws DatabaseException, ValidationException;

    /**
     * Get treatment revenue report.
     * @param startDate Start date
     * @param endDate End date
     * @return List of revenue data
     * @throws DatabaseException if database error occurs
     */
    List<BillingService.RevenueData> getTreatmentRevenueReport(LocalDate startDate, LocalDate endDate)
            throws DatabaseException;

    /**
     * Get payment report.
     * @param startDate Start date
     * @param endDate End date
     * @return List of bills with payment details
     * @throws DatabaseException if database error occurs
     */
    List<Bill> getPaymentReport(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Get patient report.
     * @param startDate Start date
     * @param endDate End date
     * @return List of patients
     * @throws DatabaseException if database error occurs
     */
    List<Patient> getPatientReport(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Get dashboard statistics.
     * @return Dashboard statistics
     * @throws DatabaseException if database error occurs
     */
    DashboardStats getDashboardStats() throws DatabaseException;

    /**
     * Inner class for dashboard statistics.
     */
    class DashboardStats {
        private long todayAppointments;
        private long completedAppointments;
        private long pendingAppointments;
        private long cancelledAppointments;
        private long totalPatients;
        private BigDecimal todayRevenue;
        private BigDecimal monthlyRevenue;

        // Getters and Setters
        public long getTodayAppointments() { return todayAppointments; }
        public void setTodayAppointments(long todayAppointments) { this.todayAppointments = todayAppointments; }
        public long getCompletedAppointments() { return completedAppointments; }
        public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }
        public long getPendingAppointments() { return pendingAppointments; }
        public void setPendingAppointments(long pendingAppointments) { this.pendingAppointments = pendingAppointments; }
        public long getCancelledAppointments() { return cancelledAppointments; }
        public void setCancelledAppointments(long cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
        public long getTotalPatients() { return totalPatients; }
        public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
        public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    }
}