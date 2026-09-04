package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.dto.BillRequestDTO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.enums.PaymentStatus;
import com.sunrise.dental.service.BillingService;
import com.sunrise.dental.service.Impl.BillingServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Billing Controller - Handles bill generation and payments.
 * Endpoints:
 * GET /api/bills - Get all bills
 * GET /api/bills/{id} - Get bill by ID or number
 * GET /api/bills/appointment/{appointmentId} - Get bill by appointment
 * POST /api/bills - Generate bill
 * POST /api/bills/{id}/pay - Process payment
 * PUT /api/bills/{id} - Update bill
 */
@WebServlet("/api/bills/*")
public class BillingController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(BillingController.class);
    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    public BillingController() {
        this.billingService = new BillingServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // GET /api/bills - Get bills with filters
            if (path == null || "/".equals(path)) {
                String statusParam = request.getParameter("status");
                String startDateParam = request.getParameter("startDate");
                String endDateParam = request.getParameter("endDate");
                String patientIdParam = request.getParameter("patientId");

                List<Bill> bills;

                if (statusParam != null) {
                    PaymentStatus status = PaymentStatus.fromString(statusParam);
                    bills = billingService.getBillsByPaymentStatus(status);
                } else if (startDateParam != null && endDateParam != null) {
                    LocalDate startDate = LocalDate.parse(startDateParam);
                    LocalDate endDate = LocalDate.parse(endDateParam);
                    bills = billingService.getBillsByDateRange(startDate, endDate);
                } else if (patientIdParam != null) {
                    bills = billingService.getBillsByPatientId(Integer.parseInt(patientIdParam));
                } else {
                    bills = billingService.getBillsByDateRange(
                            LocalDate.now().minusDays(30),
                            LocalDate.now()
                    );
                }

                sendSuccessResponse(response, bills);
                return;
            }

            // GET /api/bills/appointment/{appointmentId}
            if (path.startsWith("/appointment/")) {
                String idStr = path.substring("/appointment/".length());
                int appointmentId = Integer.parseInt(idStr);
                Bill bill = billingService.getBillByAppointmentId(appointmentId);
                sendSuccessResponse(response, bill);
                return;
            }

            // GET /api/bills/{id}
            String idStr = path.substring(1);
            try {
                int id = Integer.parseInt(idStr);
                Bill bill = billingService.getBillById(id);
                sendSuccessResponse(response, bill);
            } catch (NumberFormatException e) {
                Bill bill = billingService.getBillByNumber(idStr);
                sendSuccessResponse(response, bill);
            }

        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error in BillingController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error in BillingController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // POST /api/bills/{id}/pay - Process payment
            if (path != null && path.contains("/pay")) {
                String[] parts = path.split("/");
                int billId = Integer.parseInt(parts[1]);
                handlePayment(request, response, billId);
                return;
            }

            // POST /api/bills - Generate bill
            BillRequestDTO billRequest = objectMapper.readValue(
                    request.getReader(), BillRequestDTO.class
            );

            Bill generated = billingService.generateBill(billRequest);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(generated));

            logger.info("Bill generated: {}", generated.getBillNumber());

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error in BillingController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error in BillingController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private void handlePayment(HttpServletRequest request, HttpServletResponse response, int billId)
            throws IOException {
        try {
            Payment payment = objectMapper.readValue(request.getReader(), Payment.class);
            Bill updated = billingService.processPayment(billId, payment);
            sendSuccessResponse(response, updated);

            logger.info("Payment processed for bill: {}", billId);

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error processing payment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
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