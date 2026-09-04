package com.sunrise.dental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.dental.dto.LoginRequestDTO;
import com.sunrise.dental.dto.LoginResponseDTO;
import com.sunrise.dental.exception.AuthenticationException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.model.User;
import com.sunrise.dental.service.AuthService;
import com.sunrise.dental.service.Impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Authentication Controller - Handles login and logout.
 * Endpoints:
 * POST /api/auth/login - Login
 * POST /api/auth/logout - Logout
 * GET /api/auth/current-user - Get current user
 */
@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthController() {
        this.authService = new AuthServiceImpl();
        this.objectMapper = new ObjectMapper();
        // Register JavaTime module for LocalDateTime support
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if ("/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/logout".equals(path)) {
            handleLogout(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Endpoint not found\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if ("/current-user".equals(path)) {
            handleCurrentUser(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Endpoint not found\"}");
        }
    }

    /**
     * Handle login request.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            // Parse request body
            LoginRequestDTO loginRequest = objectMapper.readValue(request.getReader(), LoginRequestDTO.class);

            // Authenticate
            LoginResponseDTO loginResponse = authService.login(loginRequest);

            if (loginResponse.isSuccess()) {
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", loginResponse);
                session.setAttribute("userId", loginResponse.getUserId());
                session.setAttribute("username", loginResponse.getUsername());
                session.setAttribute("role", loginResponse.getRole());
                session.setMaxInactiveInterval(1800); // 30 minutes

                logger.info("User logged in: {}", loginResponse.getUsername());

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
            }

        } catch (AuthenticationException e) {
            logger.warn("Login failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");

        } catch (DatabaseException e) {
            logger.error("Database error during login", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"System error. Please try again.\"}");

        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"An unexpected error occurred.\"}");
        }
    }

    /**
     * Handle logout request.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            logger.info("User logged out: {}", username);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":true,\"message\":\"Logged out successfully\"}");
    }

    /**
     * Get current user from session.
     */
    private void handleCurrentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            LoginResponseDTO user = (LoginResponseDTO) session.getAttribute("user");
            if (user != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(user));
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"Not authenticated\"}");
    }
}