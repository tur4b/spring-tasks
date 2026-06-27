package org.example.config.security;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogoutTokenStore {

    private final Map<String, Instant> invalidatedTokens = new ConcurrentHashMap<>();

    public void invalidateToken(String token, Instant expiry) {
        invalidatedTokens.put(token, expiry);
    }

    public boolean isTokenInvalidated(String token) {
        purgeExpiredTokens();
        return invalidatedTokens.containsKey(token);
    }

    @Async
    public void purgeExpiredTokens() {
        Instant now = Instant.now();
        invalidatedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
