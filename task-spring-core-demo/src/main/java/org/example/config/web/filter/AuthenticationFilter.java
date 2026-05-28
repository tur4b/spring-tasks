package org.example.config.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.AuthRequest;
import org.example.exception.model.ErrorResponse;
import org.example.exception.model.SecurityException;
import org.example.service.api.AuthService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component("AuthenticationFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter extends OncePerRequestFilter {


    /**
     * Method-specific unsecured routes: path -> allowed methods (no auth required)
     */
    private static final Map<String, Set<HttpMethod>> UNSECURED_ROUTES = Map.ofEntries(
            Map.entry("/login", Set.of(HttpMethod.POST)),
            Map.entry("/trainees", Set.of(HttpMethod.POST)),
            Map.entry("/trainers", Set.of(HttpMethod.POST))
    );

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("AuthenticationFilter path={} method={}", request.getRequestURI(), request.getMethod());

        String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authToken != null && authToken.startsWith("Basic ")) {
            try {
                AuthRequest authRequest = AuthRequest.fromBasicAuth(authToken);
                authService.authenticate(authRequest);
            } catch (SecurityException ex){
                log.warn("Authentication failed for request {} {}: {}",
                        request.getMethod(), request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                        ex.getType(),
                        ex.getMessage(),
                        List.of()
                );

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(), errorResponse);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Check method-specific unsecured routes (exact path match only, no subpaths)
        boolean isUnsecured = UNSECURED_ROUTES.entrySet().stream()
                .anyMatch(entry -> {
                    String routePath = entry.getKey();
                    Set<HttpMethod> allowedMethods = entry.getValue();

                    // EXACT match only (not subpaths)
                    boolean pathMatches = path.equals(routePath);

                    boolean methodMatches = allowedMethods.stream()
                            .anyMatch(m -> m.name().equals(method));

                    return pathMatches && methodMatches;
                });

        if (isUnsecured) {
            log.debug("Path {} method {} is unsecured (no auth required)", path, method);
            return true;
        }

        log.debug("Path {} method {} requires authentication", path, method);
        return false;
    }
}