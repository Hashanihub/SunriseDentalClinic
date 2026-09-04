package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.Impl.AppointmentDAOImpl;
import com.sunrise.dental.dao.Impl.BillDAOImpl;
import com.sunrise.dental.dao.Impl.PatientDAOImpl;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.enums.AppointmentStatus;
import com.sunrise.dental.service.BillingService;
import com.sunrise.dental.service.Impl.BillingServiceImpl;
import com.sunrise.dental.service.ReportService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of ReportService interface.
 */
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LogManager.getLogger(ReportServiceImpl.class);
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final PatientDAO patientDAO;
    private final BillingService billingService = new BillingServiceImpl();

    public ReportServiceImpl() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.billDAO = new BillDAOImpl();
        this.patientDAO = new PatientDAOImpl();
    }

    @Override
    public List<Appointment> getDailyAppointmentReport(LocalDate date) throws DatabaseException {
        if (date == null) {
            date = LocalDate.now();
        }
        return appointmentDAO.findByDate(date);
    }

    @Override
    public List<Appointment> getDentistAppointmentReport(int dentistId, LocalDate startDate, LocalDate endDate)
            throws DatabaseException, ValidationException {
        ValidationUtil.validateId(dentistId, "Dentist");
        ValidationUtil.validateDateRange(startDate, endDate);
        return appointmentDAO.findByDentistAndDate(dentistId, startDate);
        // Note: This only gets one day. For date range, you'd need to modify the DAO.
    }

    @Override
    public List<BillingService.RevenueData> getTreatmentRevenueReport(LocalDate startDate, LocalDate endDate)
            throws DatabaseException {
        return billingService.getRevenueByTreatment(startDate, endDate);
    }

    @Override
    public List<Bill> getPaymentReport(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        return billDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Patient> getPatientReport(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        return patientDAO.findByRegistrationDateRange(startDate, endDate);
    }

    @Override
    public DashboardStats getDashboardStats() throws DatabaseException {
        DashboardStats stats = new DashboardStats();

        // Get today's date
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // Today's appointments
        List<Appointment> todayAppointments = appointmentDAO.findByDate(today);
        stats.setTodayAppointments(todayAppointments.size());

        // Completed appointments
        stats.setCompletedAppointments(appointmentDAO.countByStatus(AppointmentStatus.COMPLETED));

        // Pending appointments (Scheduled + Confirmed)
        long pending = appointmentDAO.countByStatus(AppointmentStatus.SCHEDULED) +
                appointmentDAO.countByStatus(AppointmentStatus.CONFIRMED);
        stats.setPendingAppointments(pending);

        // Cancelled appointments
        stats.setCancelledAppointments(appointmentDAO.countByStatus(AppointmentStatus.CANCELLED));

        // Total patients
        stats.setTotalPatients(patientDAO.count());

        // Today's revenue
        BigDecimal todayRevenue = billDAO.getTotalRevenue(today, today);
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // Monthly revenue
        BigDecimal monthlyRevenue = billDAO.getTotalRevenue(startOfMonth, today);
        stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        logger.info("Dashboard stats generated successfully");
        return stats;
    }
}