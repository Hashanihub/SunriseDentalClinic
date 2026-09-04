package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.service.TreatmentService;
import com.sunrise.dental.service.Impl.TreatmentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Treatment Controller - Handles treatment management operations.
 * Endpoints:
 * GET /api/treatments - Get all treatments
 * GET /api/treatments/active - Get active treatments
 * GET /api/treatments/{id} - Get treatment by ID or code
 * GET /api/treatments/search?name={name} - Search treatments
 * POST /api/treatments - Add new treatment (Admin only)
 * PUT /api/treatments/{id} - Update treatment (Admin only)
 * DELETE /api/treatments/{id} - Deactivate treatment (Admin only)
 * GET /api/treatments/{id}/cost - Get total cost
 */
@WebServlet("/api/treatments/*")
public class TreatmentController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(TreatmentController.class);
    private final TreatmentService treatmentService;
    private final ObjectMapper objectMapper;

    public TreatmentController() {
        this.treatmentService = new TreatmentServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // GET /api/treatments - Get all treatments
            if (path == null || "/".equals(path)) {
                String activeOnly = request.getParameter("active");
                List<Treatment> treatments;
                if ("true".equals(activeOnly)) {
                    treatments = treatmentService.getActiveTreatments();
                } else {
                    treatments = treatmentService.getAllTreatments();
                }
                sendSuccessResponse(response, treatments);
                return;
            }

            // GET /api/treatments/active - Get active treatments
            if ("/active".equals(path)) {
                List<Treatment> treatments = treatmentService.getActiveTreatments();
                sendSuccessResponse(response, treatments);
                return;
            }

            // GET /api/treatments/search?name=xxx
            if (path.startsWith("/search")) {
                String name = request.getParameter("name");
                List<Treatment> treatments = treatmentService.searchTreatmentsByName(name);
                sendSuccessResponse(response, treatments);
                return;
            }

            // GET /api/treatments/{id}/cost
            if (path.contains("/cost")) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[1]);
                BigDecimal cost = treatmentService.calculateTotalCost(id);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"treatmentId\":" + id + ",\"totalCost\":" + cost + "}");
                return;
            }

            // GET /api/treatments/{id}
            String idStr = path.substring(1);
            try {
                int id = Integer.parseInt(idStr);
                Treatment treatment = treatmentService.getTreatmentById(id);
                sendSuccessResponse(response, treatment);
            } catch (NumberFormatException e) {
                Treatment treatment = treatmentService.getTreatmentByCode(idStr);
                sendSuccessResponse(response, treatment);
            }

        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error in TreatmentController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error in TreatmentController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Treatment treatment = objectMapper.readValue(request.getReader(), Treatment.class);
            Treatment saved = treatmentService.addTreatment(treatment);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(saved));

            logger.info("Treatment added: {}", saved.getTreatmentCode());

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error adding treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error adding treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Treatment ID is required");
            return;
        }

        try {
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);

            Treatment treatment = objectMapper.readValue(request.getReader(), Treatment.class);
            treatment.setTreatmentId(id);

            Treatment updated = treatmentService.updateTreatment(treatment);
            sendSuccessResponse(response, updated);

            logger.info("Treatment updated: {}", updated.getTreatmentCode());

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid treatment ID");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error updating treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error updating treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Treatment ID is required");
            return;
        }

        try {
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);

            boolean deleted = treatmentService.deactivateTreatment(id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"Treatment deactivated successfully\"}");
                logger.info("Treatment deactivated: {}", id);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deactivate treatment");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid treatment ID");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error deactivating treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error deactivating treatment", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
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