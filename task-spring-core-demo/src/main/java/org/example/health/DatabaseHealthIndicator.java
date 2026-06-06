package org.example.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Custom health indicator that checks database connectivity.
 *
 * Reports to: /actuator/health/db
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private final DataSource dataSource;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Try to get a connection from the pool
            try (Connection connection = dataSource.getConnection()) {
                long duration = System.currentTimeMillis() - startTime;

                builder.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("connection_response_time_ms", duration)
                        .withDetail("status", "Connection successful");

                log.debug("Database health check: UP ({}ms)", duration);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            builder.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connection_response_time_ms", duration)
                    .withException(e)
                    .withDetail("error", e.getMessage());

            log.error("Database health check: DOWN", e);
        }
    }
}

