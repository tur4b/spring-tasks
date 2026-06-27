package org.example.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for metrics collection and export.
 *
 * Most configuration is auto-handled by Spring Boot:
 * - Micrometer auto-configured
 * - Prometheus registry auto-configured
 * - Metrics endpoint (/actuator/prometheus) auto-enabled
 *
 * This class is for any additional customization.
 */
@Configuration
public class MetricsConfig {

    /**
     * Add custom tags to all metrics.
     *
     * Tags appear in Prometheus output and can be used for filtering/grouping.
     *
     * Example: All metrics will have these tags:
     * api_requests_total{application="task-manager",service="training"} 42
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> customMetricsTags() {
        return registry -> registry.config()
                .commonTags(
                        "service", "training-management",
                        "version", "1.0"
                );
    }
}

