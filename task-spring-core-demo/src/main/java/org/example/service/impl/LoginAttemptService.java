package org.example.service.impl;

import org.example.exception.model.AccountLockedException;
import org.example.exception.model.ErrorResponse;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_SECONDS = 300;

    private final Clock clock;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void validateNotBlocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt != null && attempt.blockedUntil.isAfter(Instant.now(clock))) {
            throw new AccountLockedException(
                    "User is temporarily blocked due to failed login attempts",
                    ErrorResponse.ErrorPointer.credentials
            );
        }
    }

    public void onSuccessfulLogin(String username) {
        attempts.remove(username);
    }

    public void onFailedLogin(String username) {
        attempts.compute(username, (key, current) -> {
            Instant now = Instant.now(clock);
            if (current == null || current.blockedUntil.isBefore(now)) {
                return new Attempt(1, Instant.EPOCH);
            }

            int failures = current.failures + 1;
            if (failures >= MAX_FAILED_ATTEMPTS) {
                return new Attempt(failures, now.plusSeconds(LOCK_TIME_SECONDS));
            }
            return new Attempt(failures, current.blockedUntil);
        });
    }

    private record Attempt(int failures, Instant blockedUntil) {
    }
}
