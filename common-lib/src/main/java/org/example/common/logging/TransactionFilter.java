package org.example.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = request.getHeader(TransactionContext.TRANSACTION_HEADER);
        if (!StringUtils.hasText(transactionId)) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TransactionContext.TRANSACTION_MDC_KEY, transactionId);
        response.setHeader(TransactionContext.TRANSACTION_HEADER, transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TransactionContext.TRANSACTION_MDC_KEY);
        }
    }
}
