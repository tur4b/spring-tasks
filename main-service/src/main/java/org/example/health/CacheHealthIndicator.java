package org.example.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for application cache layer.
 *
 * Reports to: /actuator/health/cache
 */
@Component
@Slf4j
public class CacheHealthIndicator extends AbstractHealthIndicator {

    // Simulated cache statistics
    private static final Map<String, Long> cacheStats = new HashMap<>();

    static {
        cacheStats.put("entries", 1234L);
        cacheStats.put("hits", 8750L);
        cacheStats.put("misses", 1250L);
        cacheStats.put("memory_mb", 25L);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            // In production, query actual cache backend (Redis, Caffeine, etc.)
            long entries = cacheStats.getOrDefault("entries", 0L);
            long hits = cacheStats.getOrDefault("hits", 0L);
            long misses = cacheStats.getOrDefault("misses", 0L);
            long memory = cacheStats.getOrDefault("memory_mb", 0L);

            long totalRequests = hits + misses;
            double hitRate = totalRequests > 0 ? (double) hits / totalRequests * 100 : 0;

            builder.up()
                    .withDetail("cache_type", "In-Mmeory")
                    .withDetail("entries_count", entries)
                    .withDetail("hit_rate_percent", String.format("%.2f", hitRate))
                    .withDetail("cache_hits", hits)
                    .withDetail("cache_misses", misses)
                    .withDetail("estimated_memory_mb", memory)
                    .withDetail("status", "Cache operational");

            log.debug("Cache health check: UP (entries={}, hit_rate={}%)", entries, String.format("%.2f", hitRate));

        } catch (Exception e) {
            builder.down()
                    .withDetail("cache_type", "Memory")
                    .withException(e)
                    .withDetail("error", "Cache health check failed: " + e.getMessage());

            log.error("Cache health check: DOWN", e);
        }
    }
}

