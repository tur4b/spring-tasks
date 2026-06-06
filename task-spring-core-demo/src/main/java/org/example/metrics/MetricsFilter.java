package org.example.metrics;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HTTP Filter that intercepts all requests and records metrics.
 *
 * Runs for every HTTP request:
 * 1. Measures request processing time
 * 2. Records request count by endpoint
 * 3. Tracks response status codes
 *
 * The filter is registered as @Component, making it automatically
 * picked up by Spring Boot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsFilter implements Filter {

    private final CustomMetrics customMetrics;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Don't measure actuator endpoints (avoid metric recursion)
        if (requestPath.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // Record request count
        customMetrics.recordApiRequest();

        // Measure execution time
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            customMetrics.recordApiResponseTime(duration);

            log.debug("Request: {} {} - Status: {} - Time: {}ms",
                    method, requestPath, httpResponse.getStatus(), duration);
        }
    }
}

