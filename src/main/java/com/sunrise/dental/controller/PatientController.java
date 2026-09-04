package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.service.PatientService;
import com.sunrise.dental.service.Impl.PatientServiceImpl;
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
 * Patient Controller - Handles patient CRUD operations.
 * Endpoints:
 * GET /api/patients - Get all patients
 * GET /api/patients/{id} - Get patient by ID
 * GET /api/patients/search?name={name} - Search patients
 * POST /api/patients - Register new patient
 * PUT /api/patients/{id} - Update patient
 * DELETE /api/patients/{id} - Deactivate patient
 */
@WebServlet("/api/patients/*")
public class PatientController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(PatientController.class);
    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    public PatientController() {
        this.patientService = new PatientServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            // GET /api/patients - Get all patients
            if (path == null || "/".equals(path)) {
                List<Patient> patients = patientService.getActivePatients();
                sendSuccessResponse(response, patients);
                return;
            }

            // GET /api/patients/search?name=xxx
            if (path.startsWith("/search")) {
                String name = request.getParameter("name");
                List<Patient> patients = patientService.searchPatientsByName(name);
                sendSuccessResponse(response, patients);
                return;
            }

            // GET /api/patients/{id}
            String idStr = path.substring(1);
            try {
                int id = Integer.parseInt(idStr);
                Patient patient = patientService.getPatientById(id);
                sendSuccessResponse(response, patient);
            } catch (NumberFormatException e) {
                // Try by patient number
                Patient patient = patientService.getPatientByNumber(idStr);
                sendSuccessResponse(response, patient);
            }

        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error in PatientController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error in PatientController", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Patient patient = objectMapper.readValue(request.getReader(), Patient.class);
            Patient saved = patientService.registerPatient(patient);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(saved));

            logger.info("Patient registered: {}", saved.getPatientNumber());

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error registering patient", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error registering patient", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Patient ID is required");
            return;
        }

        try {
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);

            Patient patient = objectMapper.readValue(request.getReader(), Patient.class);
            patient.setPatientId(id);

            Patient updated = patientService.updatePatient(patient);
            sendSuccessResponse(response, updated);

            logger.info("Patient updated: {}", updated.getPatientNumber());

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error updating patient", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error updating patient", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Patient ID is required");
            return;
        }

        try {
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);

            boolean deleted = patientService.deactivatePatient(id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"Patient deactivated successfully\"}");
                logger.info("Patient deactivated: {}", id);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deactivate patient");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error deactivating patient", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error deactivating patient", e);
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