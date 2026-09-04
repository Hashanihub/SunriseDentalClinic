package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.service.BillingService;
import com.sunrise.dental.service.ReportService;
import com.sunrise.dental.service.Impl.BillingServiceImpl;
import com.sunrise.dental.service.Impl.ReportServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report Controller - Handles report generation.
 * Endpoints:
 * GET /api/reports/daily-appointments?date={date}
 * GET /api/reports/dentist-appointments?dentistId={id}&startDate={date}&endDate={date}
 * GET /api/reports/revenue?startDate={date}&endDate={date}
 * GET /api/reports/payments?startDate={date}&endDate={date}
 * GET /api/reports/patients?startDate={date}&endDate={date}
 * GET /api/reports/dashboard-stats
 */
@WebServlet("/api/reports/*")
public class ReportController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(ReportController.class);
    private final ReportService reportService;
    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    public ReportController() {
        this.reportService = new ReportServiceImpl();
        this.billingService = new BillingServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // GET /api/reports/dashboard-stats
            if ("/dashboard-stats".equals(path)) {
                ReportService.DashboardStats stats = reportService.getDashboardStats();
                sendSuccessResponse(response, stats);
                return;
            }

            // GET /api/reports/daily-appointments
            if (path.startsWith("/daily-appointments")) {
                handleDailyAppointments(request, response);
                return;
            }

            // GET /api/reports/dentist-appointments
            if (path.startsWith("/dentist-appointments")) {
                handleDentistAppointments(request, response);
                return;
            }

            // GET /api/reports/revenue
            if (path.startsWith("/revenue")) {
                handleRevenueReport(request, response);
                return;
            }

            // GET /api/reports/payments
            if (path.startsWith("/payments")) {
                handlePaymentsReport(request, response);
                return;
            }

            // GET /api/reports/patients
            if (path.startsWith("/patients")) {
                handlePatientsReport(request, response);
                return;
            }

            // GET /api/reports/print/{billNumber} - Printer-friendly bill
            if (path.startsWith("/print/")) {
                String billNumber = path.substring("/print/".length());
                handlePrintBill(request, response, billNumber);
                return;
            }

            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Report type not found");

        } catch (DatabaseException e) {
            logger.error("Database error in ReportController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in ReportController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private void handleDailyAppointments(HttpServletRequest request, HttpServletResponse response)
            throws IOException, DatabaseException {
        String dateParam = request.getParameter("date");
        LocalDate date = dateParam != null ? LocalDate.parse(dateParam) : LocalDate.now();

        List<Appointment> appointments = reportService.getDailyAppointmentReport(date);
        sendSuccessResponse(response, appointments);
    }

    private void handleDentistAppointments(HttpServletRequest request, HttpServletResponse response)
            throws IOException, DatabaseException, ValidationException {
        String dentistIdParam = request.getParameter("dentistId");
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        if (dentistIdParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "dentistId is required");
            return;
        }

        int dentistId = Integer.parseInt(dentistIdParam);
        LocalDate startDate = startDateParam != null ? LocalDate.parse(startDateParam) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateParam != null ? LocalDate.parse(endDateParam) : LocalDate.now();

        List<Appointment> appointments = reportService.getDentistAppointmentReport(dentistId, startDate, endDate);
        sendSuccessResponse(response, appointments);
    }

    private void handleRevenueReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException, DatabaseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        LocalDate startDate = startDateParam != null ? LocalDate.parse(startDateParam) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateParam != null ? LocalDate.parse(endDateParam) : LocalDate.now();

        List<BillingService.RevenueData> revenueData = billingService.getRevenueByTreatment(startDate, endDate);
        sendSuccessResponse(response, revenueData);
    }

    private void handlePaymentsReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException, DatabaseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        LocalDate startDate = startDateParam != null ? LocalDate.parse(startDateParam) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateParam != null ? LocalDate.parse(endDateParam) : LocalDate.now();

        List<Bill> bills = reportService.getPaymentReport(startDate, endDate);
        sendSuccessResponse(response, bills);
    }

    private void handlePatientsReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException, DatabaseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        LocalDate startDate = startDateParam != null ? LocalDate.parse(startDateParam) : LocalDate.now().minusDays(30);
        LocalDate endDate = endDateParam != null ? LocalDate.parse(endDateParam) : LocalDate.now();

        List<Patient> patients = reportService.getPatientReport(startDate, endDate);
        sendSuccessResponse(response, patients);
    }

    private void handlePrintBill(HttpServletRequest request, HttpServletResponse response, String billNumber)
            throws IOException, DatabaseException {

        Bill bill = null;
        try {
            bill = billingService.getBillByNumber(billNumber);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Bill - " + billNumber + "</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
        out.println(".header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; }");
        out.println(".clinic-name { font-size: 24px; font-weight: bold; color: #2c3e50; }");
        out.println(".clinic-address { font-size: 14px; color: #666; }");
        out.println(".bill-title { font-size: 20px; font-weight: bold; margin: 20px 0; }");
        out.println(".bill-details { margin: 20px 0; }");
        out.println(".bill-details table { width: 100%; border-collapse: collapse; }");
        out.println(".bill-details td { padding: 8px; }");
        out.println(".bill-details .label { font-weight: bold; width: 150px; }");
        out.println(".items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        out.println(".items-table th { background-color: #2c3e50; color: white; padding: 10px; text-align: left; }");
        out.println(".items-table td { padding: 10px; border-bottom: 1px solid #ddd; }");
        out.println(".total-section { margin-top: 20px; border-top: 2px solid #333; padding-top: 20px; }");
        out.println(".total-section table { width: 100%; }");
        out.println(".total-section td { padding: 5px; }");
        out.println(".grand-total { font-size: 18px; font-weight: bold; color: #2c3e50; }");
        out.println(".footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }");
        out.println(".status-paid { color: green; font-weight: bold; }");
        out.println(".status-unpaid { color: red; font-weight: bold; }");
        out.println(".status-partial { color: orange; font-weight: bold; }");
        out.println("@media print { .no-print { display: none; } }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        // Header
        out.println("<div class='header'>");
        out.println("<div class='clinic-name'>☀️ Sunrise Dental Clinic</div>");
        out.println("<div class='clinic-address'>Colombo, Sri Lanka | Tel: 077-123-4567</div>");
        out.println("</div>");

        // Bill Title
        out.println("<div class='bill-title'>Dental Bill / Receipt</div>");

        // Bill Details
        out.println("<div class='bill-details'>");
        out.println("<table>");
        out.println("<tr><td class='label'>Bill Number:</td><td>" + bill.getBillNumber() + "</td></tr>");
        out.println("<tr><td class='label'>Date:</td><td>" + bill.getBillDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</td></tr>");
        out.println("<tr><td class='label'>Patient:</td><td>" + (bill.getPatient() != null ? bill.getPatient().getFullName() : "N/A") + "</td></tr>");
        out.println("<tr><td class='label'>Appointment:</td><td>" + (bill.getAppointment() != null ? bill.getAppointment().getAppointmentNumber() : "N/A") + "</td></tr>");
        out.println("<tr><td class='label'>Status:</td><td class='status-" + bill.getPaymentStatus().name().toLowerCase() + "'>" + bill.getPaymentStatus().getDisplayName() + "</td></tr>");
        out.println("</table>");
        out.println("</div>");

        // Items
        out.println("<table class='items-table'>");
        out.println("<thead>");
        out.println("<tr><th>Description</th><th style='text-align:right'>Amount (LKR)</th></tr>");
        out.println("</thead>");
        out.println("<tbody>");
        out.println("<tr><td>Treatment Cost</td><td style='text-align:right'>" + bill.getTreatmentCost() + "</td></tr>");
        out.println("<tr><td>Consultation Fee</td><td style='text-align:right'>" + bill.getConsultationFee() + "</td></tr>");
        if (bill.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            out.println("<tr><td>Discount</td><td style='text-align:right'>- " + bill.getDiscount() + "</td></tr>");
        }
        if (bill.getTax().compareTo(java.math.BigDecimal.ZERO) > 0) {
            out.println("<tr><td>Tax</td><td style='text-align:right'>" + bill.getTax() + "</td></tr>");
        }
        out.println("</tbody>");
        out.println("</table>");

        // Total
        out.println("<div class='total-section'>");
        out.println("<table>");
        out.println("<tr><td style='text-align:right'><strong>Subtotal:</strong></td><td style='text-align:right;width:150px'>LKR " + bill.getSubtotal() + "</td></tr>");
        out.println("<tr><td style='text-align:right'><strong>Total:</strong></td><td style='text-align:right;width:150px' class='grand-total'>LKR " + bill.getTotalAmount() + "</td></tr>");
        out.println("<tr><td style='text-align:right'><strong>Paid:</strong></td><td style='text-align:right;width:150px'>LKR " + bill.getPaidAmount() + "</td></tr>");
        out.println("<tr><td style='text-align:right'><strong>Balance:</strong></td><td style='text-align:right;width:150px'>LKR " + bill.getBalanceDue() + "</td></tr>");
        out.println("</table>");
        out.println("</div>");

        // Notes
        if (bill.getNotes() != null && !bill.getNotes().trim().isEmpty()) {
            out.println("<div style='margin-top:20px;padding:10px;background:#f9f9f9;border-radius:5px;'>");
            out.println("<strong>Notes:</strong> " + bill.getNotes());
            out.println("</div>");
        }

        // Footer
        out.println("<div class='footer'>");
        out.println("<p>Thank you for choosing Sunrise Dental Clinic</p>");
        out.println("<p>This is a computer-generated receipt. Please retain for your records.</p>");
        out.println("<button class='no-print' onclick='window.print()' style='padding:10px 20px;margin-top:10px;background:#2c3e50;color:white;border:none;border-radius:5px;cursor:pointer;'>🖨️ Print / Save as PDF</button>");
        out.println("<button class='no-print' onclick='window.close()' style='padding:10px 20px;margin-top:10px;margin-left:10px;background:#e74c3c;color:white;border:none;border-radius:5px;cursor:pointer;'>✕ Close</button>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    private void sendSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(data));
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}