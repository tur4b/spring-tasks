package org.example.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generating IDs
 */
@Component
public class IdGenerator {

    private static final Map<String, AtomicLong> ID_GENERATORS = new ConcurrentHashMap<>();

    /**
     * Get next id of entity
     *
     * @param identifier we take identifier
     * @return long id value
     */
    public Long getNextId(String identifier) {
        return ID_GENERATORS.computeIfAbsent(identifier, key -> new AtomicLong(1))
                .getAndIncrement();
    }

}
