package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sunrise.dental.dto.AppointmentRequestDTO;
import com.sunrise.dental.exception.AppointmentConflictException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.enums.AppointmentStatus;
import com.sunrise.dental.service.AppointmentService;
import com.sunrise.dental.service.Impl.AppointmentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@WebServlet("/api/appointments/*")
public class AppointmentController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AppointmentController.class);
    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    public AppointmentController() {
        this.appointmentService = new AppointmentServiceImpl();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        HttpSession session = request.getSession(false);
        String userRole = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");

        try {
            // ----- GET /api/appointments -----
            if (path == null || "/".equals(path)) {
                String dateParam = request.getParameter("date");
                String statusParam = request.getParameter("status");
                String patientIdParam = request.getParameter("patientId");
                String dentistIdParam = request.getParameter("dentistId");

                List<Appointment> appointments;

                // DENTIST: Only their own appointments
                if ("DENTIST".equals(userRole)) {
                    Integer dentistId = getDentistIdFromUser(userId);
                    appointments = appointmentService.getAppointmentsByDentistId(dentistId);

                    if (dateParam != null) {
                        LocalDate date = LocalDate.parse(dateParam);
                        appointments = appointments.stream()
                                .filter(a -> a.getAppointmentDate().equals(date))
                                .toList();
                    }
                    if (statusParam != null) {
                        AppointmentStatus status = AppointmentStatus.fromString(statusParam);
                        appointments = appointments.stream()
                                .filter(a -> a.getStatus() == status)
                                .toList();
                    }
                    sendSuccessResponse(response, appointments);
                    return;
                }

                // ADMIN / RECEPTIONIST: All appointments
                if (dateParam != null) {
                    appointments = appointmentService.getAppointmentsByDate(LocalDate.parse(dateParam));
                } else if (statusParam != null) {
                    appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.fromString(statusParam));
                } else if (patientIdParam != null) {
                    appointments = appointmentService.getAppointmentsByPatientId(Integer.parseInt(patientIdParam));
                } else if (dentistIdParam != null) {
                    appointments = appointmentService.getAppointmentsByDentistId(Integer.parseInt(dentistIdParam));
                } else {
                    appointments = appointmentService.getTodayAppointments();
                }
                sendSuccessResponse(response, appointments);
                return;
            }

            // ----- GET /api/appointments/search?number=xxx -----
            if (path.startsWith("/search")) {
                String number = request.getParameter("number");
                if (number == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Appointment number required");
                    return;
                }
                Appointment appointment = appointmentService.getAppointmentByNumber(number);

                // DENTIST: Check ownership
                if ("DENTIST".equals(userRole)) {
                    Integer dentistId = getDentistIdFromUser(userId);
                    if (appointment.getDentistId() != dentistId) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Not your appointment");
                        return;
                    }
                }
                sendSuccessResponse(response, appointment);
                return;
            }

            // ----- GET /api/appointments/{id} -----
            String idStr = path.substring(1);
            Appointment appointment;
            try {
                int id = Integer.parseInt(idStr);
                appointment = appointmentService.getAppointmentById(id);
            } catch (NumberFormatException e) {
                appointment = appointmentService.getAppointmentByNumber(idStr);
            }

            // DENTIST: Check ownership
            if ("DENTIST".equals(userRole)) {
                Integer dentistId = getDentistIdFromUser(userId);
                if (appointment.getDentistId() != dentistId) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Not your appointment");
                    return;
                }
            }
            sendSuccessResponse(response, appointment);

        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String userRole = (String) session.getAttribute("role");

        if ("DENTIST".equals(userRole)) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Dentists cannot create appointments");
            return;
        }

        try {
            AppointmentRequestDTO dto = objectMapper.readValue(request.getReader(), AppointmentRequestDTO.class);
            Appointment saved = appointmentService.createAppointment(dto);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(objectMapper.writeValueAsString(saved));
            logger.info("Appointment created: {}", saved.getAppointmentNumber());
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (AppointmentConflictException e) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Appointment ID required");
            return;
        }

        HttpSession session = request.getSession(false);
        String userRole = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");

        try {
            String[] parts = path.substring(1).split("/");
            int id = Integer.parseInt(parts[0]);

            // DENTIST: Check ownership
            if ("DENTIST".equals(userRole)) {
                Appointment existing = appointmentService.getAppointmentById(id);
                Integer dentistId = getDentistIdFromUser(userId);
                if (existing.getDentistId() != dentistId) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Not your appointment");
                    return;
                }
            }

            // PUT /api/appointments/{id}/status
            if (parts.length > 1 && "status".equals(parts[1])) {
                handleStatusUpdate(request, response, id, userRole);
                return;
            }

            // PUT /api/appointments/{id}/reschedule - Only ADMIN/RECEPTIONIST
            if (parts.length > 1 && "reschedule".equals(parts[1])) {
                if ("DENTIST".equals(userRole)) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Dentists cannot reschedule");
                    return;
                }
                handleReschedule(request, response, id);
                return;
            }

            // PUT /api/appointments/{id} - Full update (ADMIN/RECEPTIONIST only)
            if ("DENTIST".equals(userRole)) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Dentists cannot update full appointment");
                return;
            }

            Appointment appointment = objectMapper.readValue(request.getReader(), Appointment.class);
            appointment.setAppointmentId(id);
            Appointment updated = appointmentService.updateAppointment(appointment);
            sendSuccessResponse(response, updated);
            logger.info("Appointment updated: {}", updated.getAppointmentNumber());

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (AppointmentConflictException e) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String userRole = (String) session.getAttribute("role");

        if ("DENTIST".equals(userRole)) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Dentists cannot cancel appointments");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Appointment ID required");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            String reason = request.getParameter("reason");
            if (reason == null || reason.trim().isEmpty()) reason = "Cancelled by user";

            if (appointmentService.cancelAppointment(id, reason)) {
                response.getWriter().write("{\"success\":true,\"message\":\"Appointment cancelled\"}");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to cancel");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    // ==================== PRIVATE METHODS ====================

    private void handleStatusUpdate(HttpServletRequest request, HttpServletResponse response, int id, String userRole)
            throws IOException {
        try {
            String statusParam = request.getParameter("status");
            if (statusParam == null || statusParam.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Status required");
                return;
            }

            // DENTIST: Only allowed statuses
            if ("DENTIST".equals(userRole)) {
                List<String> allowed = Arrays.asList("COMPLETED", "CONFIRMED", "NO_SHOW");
                if (!allowed.contains(statusParam)) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                            "Dentists can only set: COMPLETED, CONFIRMED, NO_SHOW");
                    return;
                }
            }

            AppointmentStatus status = AppointmentStatus.fromString(statusParam);
            if (appointmentService.updateAppointmentStatus(id, status)) {
                response.getWriter().write("{\"success\":true,\"message\":\"Status updated\"}");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update");
            }
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }

    private void handleReschedule(HttpServletRequest request, HttpServletResponse response, int id)
            throws IOException {
        try {
            String dateParam = request.getParameter("date");
            String timeParam = request.getParameter("time");
            if (dateParam == null || timeParam == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Date and time required");
                return;
            }

            Appointment rescheduled = appointmentService.rescheduleAppointment(
                    id, LocalDate.parse(dateParam), LocalTime.parse(timeParam));
            sendSuccessResponse(response, rescheduled);
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
        } catch (AppointmentConflictException e) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, e.getUserMessage());
        } catch (ResourceNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getUserMessage());
        } catch (DatabaseException e) {
            logger.error("Database error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private Integer getDentistIdFromUser(Integer userId) {
        // In real system: SELECT dentist_id FROM dentists WHERE user_id = ?
        return userId; // For demo: dentist_id = user_id
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