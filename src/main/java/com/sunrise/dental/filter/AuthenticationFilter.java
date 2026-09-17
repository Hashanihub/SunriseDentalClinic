package com.sunrise.dental.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication Filter - Role-based access control
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean isAuthenticated = session != null && session.getAttribute("user") != null;
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // ----- PUBLIC ACCESS -----
        if (requestURI.endsWith("/login.html") ||
                requestURI.endsWith("/login") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/images/") ||
                requestURI.endsWith("/error.html") ||
                requestURI.endsWith("/index.html") ||
                requestURI.contains("/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        // ----- CHECK AUTHENTICATION -----
        if (!isAuthenticated) {
            if (requestURI.contains("/api/")) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Unauthorized. Please login.\"}");
                return;
            }
            httpResponse.sendRedirect(contextPath + "/login.html");
            return;
        }

        // ----- GET USER ROLE -----
        String userRole = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");

        // ============================================================
        // ===== DENTIST RESTRICTIONS =====
        // ============================================================
        if ("DENTIST".equals(userRole)) {

            // ----- RESTRICTED PAGES (HTML) -----
            List<String> restrictedPages = Arrays.asList(
                    "/patients.html",
                    "/dentists.html",
                    "/treatments.html",
                    "/billing.html",
                    "/reports.html",
                    "/help.html",
                    "/appointments.html"
            );

            for (String restricted : restrictedPages) {
                if (requestURI.contains(restricted)) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("text/html");
                    httpResponse.getWriter().write(getAccessDeniedPage(contextPath));
                    return;
                }
            }

            // ----- RESTRICTED API ENDPOINTS -----
            List<String> restrictedApi = Arrays.asList(
                    "/api/patients",
                    "/api/dentists",
                    "/api/treatments",
                    "/api/bills",
                    "/api/reports"
            );

            for (String restricted : restrictedApi) {
                if (requestURI.contains(restricted)) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Access denied for Dentists.\"}");
                    return;
                }
            }

            // ✅ Dentist CANNOT create appointments
            if (requestURI.contains("/api/appointments") &&
                    httpRequest.getMethod().equals("POST")) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"error\":\"Dentists cannot create appointments.\"}");
                return;
            }

            // ✅ Dentist CAN view appointments (GET only)
            if (requestURI.contains("/api/appointments") &&
                    httpRequest.getMethod().equals("GET")) {
                chain.doFilter(request, response);
                return;
            }
            // ============================================================
// ===== USERS API - ADMIN ONLY =====
// ============================================================
            if (requestURI.contains("/api/users") || requestURI.contains("/users.html")) {
                if (!"ADMIN".equals(userRole)) {
                    if (requestURI.contains("/api/")) {
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.setContentType("application/json");
                        httpResponse.getWriter().write("{\"error\":\"Admin access required.\"}");
                    } else {
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.setContentType("text/html");
                        httpResponse.getWriter().write(getAccessDeniedPage(contextPath));
                    }
                    return;
                }
            }
        }

        // ============================================================
        // ===== RECEPTIONIST RESTRICTIONS =====
        // ============================================================
        if ("RECEPTIONIST".equals(userRole)) {
            String method = httpRequest.getMethod();

            // Receptionists cannot modify dentists or treatments
            if ((requestURI.contains("/api/dentists") || requestURI.contains("/api/treatments")) &&
                    (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Access denied for Receptionists.\"}");
                return;
            }
        }

        // ----- ADMIN: Full Access - Allow all -----

        // ----- PROCEED -----
        chain.doFilter(request, response);
    }

    private String getAccessDeniedPage(String contextPath) {
        return "<!DOCTYPE html>" +
                "<html><head><title>Access Denied</title>" +
                "<style>body{font-family:Arial;text-align:center;padding:50px;background:#f5f5f5;}" +
                ".error{color:#e74c3c;font-size:60px;}" +
                ".msg{font-size:18px;color:#555;margin:20px 0;}" +
                "a{color:#1a5276;text-decoration:none;font-weight:bold;}" +
                "a:hover{text-decoration:underline;}</style>" +
                "</head><body>" +
                "<div class='error'>&#128274;</div>" +
                "<h1 style='color:#2c3e50;'>Access Denied</h1>" +
                "<p class='msg'>You do not have permission to access this page.</p>" +
                "<p><a href='" + contextPath + "/dashboard.html'>← Back to Dashboard</a></p>" +
                "</body></html>";
    }

    @Override
    public void destroy() {
    }
}