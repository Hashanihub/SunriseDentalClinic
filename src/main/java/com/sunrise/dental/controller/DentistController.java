package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.enums.AvailabilityStatus;
import com.sunrise.dental.service.DentistService;
import com.sunrise.dental.service.Impl.DentistServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Dentist Controller - Handles dentist management operations.
 * Endpoints:
 * GET /api/dentists - Get all dentists
 * GET /api/dentists/available - Get available dentists
 * GET /api/dentists/{id} - Get dentist by ID or number
 * GET /api/dentists/search?name={name} - Search dentists
 * POST /api/dentists - Add new dentist (Admin only)
 * PUT /api/dentists/{id} - Update dentist (Admin only)
 * PUT /api/dentists/{id}/availability - Update availability status (Admin only)
 * DELETE /api/dentists/{id} - Deactivate dentist (Admin only)
 */
@WebServlet("/api/dentists/*")
public class DentistController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(DentistController.class);
    private final DentistService dentistService;
    private final ObjectMapper objectMapper;

    public DentistController() {
        this.dentistService = new DentistServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // GET /api/dentists - Get all dentists
            if (path == null || "/".equals(path)) {
                String activeOnly = request.getParameter("active");
                List<Dentist> dentists;
                if ("true".equals(activeOnly)) {
                    dentists = dentistService.getActiveDentists();
                } else {
                    dentists = dentistService.getAllDentists();
                }
                sendSuccessResponse(response, dentists);
                return;
            }

            // GET /api/dentists/available - Get available dentists
            if ("/available".equals(path)) {
                List<Dentist> dentists = dentistService.getAvailableDentists();
                sendSuccessResponse(response, dentists);
                return;
            }

            // GET /api/dentists/search?name=xxx
            if (path.startsWith("/search")) {
                String name = request.getParameter("name");
                List<Dentist> dentists = dentistService.searchDentistsByName(name);
                sendSuccessResponse(response, dentists);
                return;
            }

            // GET /api/dentists/specialization?spec=xxx
            if (path.startsWith("/specialization")) {
                String spec = request.getParameter("spec");
                List<Dentist> dentists = dentistService.getDentistsBySpecialization(spec);
                sendSuccessResponse(response, dentists);
                return;
            }

            // GET /api/dentists/{id}
            String idStr = path.substring(1);
            try {
                int id = Integer.parseInt(idStr);
                Dentist dentist = dentistService.getDentistById(id);
                sendSuccessResponse(response, dentist);
            } catch (NumberFormatException e) {
                Dentist dentist = dentistService.getDentistByNumber(idStr);
                sendSuccessResponse(response, dentist);
            }

        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error in DentistController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error in DentistController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Check admin role - handled by filter
            Dentist dentist = objectMapper.readValue(request.getReader(), Dentist.class);
            Dentist saved = dentistService.addDentist(dentist);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(saved));

            logger.info("Dentist added: {}", saved.getDentistNumber());

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error adding dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error adding dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Dentist ID is required");
            return;
        }

        try {
            String[] pathParts = path.substring(1).split("/");
            int id = Integer.parseInt(pathParts[0]);

            // PUT /api/dentists/{id}/availability
            if (pathParts.length > 1 && "availability".equals(pathParts[1])) {
                handleAvailabilityUpdate(request, response, id);
                return;
            }

            // PUT /api/dentists/{id} - Update dentist
            Dentist dentist = objectMapper.readValue(request.getReader(), Dentist.class);
            dentist.setDentistId(id);

            Dentist updated = dentistService.updateDentist(dentist);
            sendSuccessResponse(response, updated);

            logger.info("Dentist updated: {}", updated.getDentistNumber());

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid dentist ID");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error updating dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error updating dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Dentist ID is required");
            return;
        }

        try {
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);

            boolean deleted = dentistService.deactivateDentist(id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"Dentist deactivated successfully\"}");
                logger.info("Dentist deactivated: {}", id);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deactivate dentist");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid dentist ID");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error deactivating dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error deactivating dentist", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private void handleAvailabilityUpdate(HttpServletRequest request, HttpServletResponse response, int id)
            throws IOException {
        try {
            String statusParam = request.getParameter("status");
            if (statusParam == null || statusParam.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Status is required");
                return;
            }

            AvailabilityStatus status = AvailabilityStatus.fromString(statusParam);
            boolean updated = dentistService.updateDentistAvailability(id, status);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"Availability updated successfully\"}");
                logger.info("Dentist availability updated: {} -> {}", id, status);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update availability");
            }

        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid status value");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error updating availability", e);
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