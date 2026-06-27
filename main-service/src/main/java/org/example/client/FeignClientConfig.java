package org.example.client;

import feign.RequestInterceptor;
import org.example.common.logging.TransactionContext;
import org.example.common.security.JwtService;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor workloadRequestInterceptor(JwtService jwtService) {
        return template -> {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken("main-service"));
            String transactionId = MDC.get(TransactionContext.TRANSACTION_MDC_KEY);
            if (transactionId != null) {
                template.header(TransactionContext.TRANSACTION_HEADER, transactionId);
            }
        };
    }
}
