package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.model.enums.UserRole;
import com.sunrise.dental.service.UserService;
import com.sunrise.dental.service.Impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * User Controller - Admin only
 * Handles user management (create, update, deactivate)
 */
@WebServlet("/api/users/*")
public class UserController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController() {
        this.userService = new UserServiceImpl();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Check if user is ADMIN
     */
    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Please login first");
            return false;
        }
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) return;

        String path = request.getPathInfo();

        try {
            // GET /api/users - Get all users
            if (path == null || "/".equals(path)) {
                List<User> users = userService.getAllUsers();
                sendSuccessResponse(response, users);
                return;
            }

            // GET /api/users/{id} - Get user by ID
            String idStr = path.substring(1);
            int id = Integer.parseInt(idStr);
            User user = userService.getUserById(id);
            sendSuccessResponse(response, user);

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
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

        if (!isAdmin(request, response)) return;

        try {
            User user = objectMapper.readValue(request.getReader(), User.class);
            User saved = userService.createUser(user);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(saved));
            logger.info("User created: {}", saved.getUsername());

        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
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

        if (!isAdmin(request, response)) return;

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "User ID required");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            User user = objectMapper.readValue(request.getReader(), User.class);
            user.setUserId(id);
            User updated = userService.updateUser(user);
            sendSuccessResponse(response, updated);
            logger.info("User updated: {}", updated.getUsername());

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        } catch (ValidationException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getUserMessage());
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

        if (!isAdmin(request, response)) return;

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "User ID required");
            return;
        }

        try {
            int id = Integer.parseInt(path.substring(1));
            boolean deleted = userService.deactivateUser(id);
            if (deleted) {
                response.getWriter().write("{\"success\":true,\"message\":\"User deactivated\"}");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to deactivate");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
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