package org.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom Prometheus metrics definitions.
 */
@Component
@Slf4j
public class CustomMetrics {

    @Getter
    private final Counter apiRequestsTotal;

    @Getter
    private final Counter trainingAssignmentsCreated;

    @Getter
    private final Counter loginAttemptsSuccess;

    @Getter
    private final Counter loginAttemptsFailed;

    @Getter
    private final Timer apiResponseTime;

    @Getter
    private final Timer databaseQueryTime;

    @Getter
    private final Timer authenticationTime;

    public CustomMetrics(MeterRegistry meterRegistry) {

        this.apiRequestsTotal = Counter.builder("api.requests.total")
                .description("Total API requests")
                .baseUnit("requests")
                .register(meterRegistry);

        this.trainingAssignmentsCreated = Counter.builder("training.assignments.created")
                .description("Total training assignments created")
                .baseUnit("assignments")
                .register(meterRegistry);

        this.loginAttemptsSuccess = Counter.builder("login.attempts.success")
                .description("Successful login attempts")
                .baseUnit("attempts")
                .register(meterRegistry);

        this.loginAttemptsFailed = Counter.builder("login.attempts.failed")
                .description("Failed login attempts")
                .baseUnit("attempts")
                .register(meterRegistry);

        this.apiResponseTime = Timer.builder("api.response.time")
                .description("API response time")
//                .baseUnit("milliseconds")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)  // Track percentiles
                .register(meterRegistry);

        this.databaseQueryTime = Timer.builder("database.query.time")
                .description("Database query execution time")
//                .baseUnit("milliseconds")
                .register(meterRegistry);

        this.authenticationTime = Timer.builder("authentication.time")
                .description("Authentication processing time")
//                .baseUnit("milliseconds")
                .register(meterRegistry);

        log.info("Custom metrics initialized with MeterRegistry");
    }

    public void recordApiRequest() {
        apiRequestsTotal.increment();
    }

    public void recordLoginSuccess() {
        loginAttemptsSuccess.increment();
    }

    public void recordLoginFailure() {
        loginAttemptsFailed.increment();
    }

    public Timer.Sample startApiResponseTimer() {
        return Timer.start();
    }

    public void recordApiResponseTime(long durationMs) {
        apiResponseTime.record(java.time.Duration.ofMillis(durationMs));
    }
}

