package org.example.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_LOG_LENGTH = 2000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            String transactionId = MDC.get(TransactionContext.TRANSACTION_MDC_KEY);
            String method = requestWrapper.getMethod();
            String uri = requestWrapper.getRequestURI();
            String query = requestWrapper.getQueryString();
            int status = responseWrapper.getStatus();
            String requestBody = toSafeString(requestWrapper.getContentAsByteArray());
            String responseBody = toSafeString(responseWrapper.getContentAsByteArray());
            String responseMessage = extractMessage(responseBody);

            log.info(
                    "TRANSACTION transactionId={} endpoint={} method={} query={} status={} requestBody={} responseMessage={}",
                    transactionId,
                    uri,
                    method,
                    query,
                    status,
                    maskSensitive(requestBody),
                    responseMessage
            );

            responseWrapper.copyBodyToResponse();
        }
    }

    private String toSafeString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String value = new String(bytes, StandardCharsets.UTF_8);
        if (value.length() > MAX_LOG_LENGTH) {
            return value.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
        }
        return value;
    }

    private String extractMessage(String responseBody) {
        String token = "\"message\":\"";
        int start = responseBody.indexOf(token);
        if (start < 0) {
            return "";
        }
        int from = start + token.length();
        int end = responseBody.indexOf('"', from);
        if (end < 0) {
            return "";
        }
        return responseBody.substring(from, end);
    }

    private String maskSensitive(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body
                .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"oldPassword\"\\s*:\\s*\"[^\"]*\"", "\"oldPassword\":\"***\"")
                .replaceAll("(?i)\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"***\"");
    }
}
