package com.sunrise.dental.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to set character encoding for all requests and responses.
 */
public class CharacterEncodingFilter implements Filter {

    private String encoding = "UTF-8";
    private boolean forceEncoding = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null) {
            encoding = encodingParam;
        }
        String forceParam = filterConfig.getInitParameter("forceEncoding");
        if (forceParam != null) {
            forceEncoding = Boolean.parseBoolean(forceParam);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set request encoding
        if (forceEncoding || httpRequest.getCharacterEncoding() == null) {
            httpRequest.setCharacterEncoding(encoding);
        }

        // Set response encoding
        if (forceEncoding || httpResponse.getCharacterEncoding() == null) {
            httpResponse.setCharacterEncoding(encoding);
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}