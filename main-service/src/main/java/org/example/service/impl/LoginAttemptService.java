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
    private final Map<String, Attempt> ipAttempts = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void validateNotBlocked(String ipAddress) {
        checkNotBlocked(ipAddress);
    }

    public void onSuccessfulLogin(String ipAddress) {
        ipAttempts.remove(ipAddress);
    }

    public void onFailedLogin(String ipAddress) {
        recordAttempt(ipAddress);
    }

    private void checkNotBlocked(String ipAddress) {
        Attempt attempt = ipAttempts.get(ipAddress);
        if (attempt != null && attempt.blockedUntil().isAfter(Instant.now(clock))) {
            throw new AccountLockedException(
                    "Access temporarily blocked due to too many failed login attempts",
                    ErrorResponse.ErrorPointer.credentials
            );
        }
    }

    private void recordAttempt(String ipAddress) {
        ipAttempts.compute(ipAddress, (k, current) -> {
            Instant now = Instant.now(clock);
            if (current == null) {
                return new Attempt(1, Instant.EPOCH);
            }

            // If a previous block window expired, start a fresh failure window.
            if (current.blockedUntil().isAfter(Instant.EPOCH) && current.blockedUntil().isBefore(now)) {
                return new Attempt(1, Instant.EPOCH);
            }

            int failures = current.failures() + 1;
            if (failures >= MAX_FAILED_ATTEMPTS) {
                return new Attempt(failures, now.plusSeconds(LOCK_TIME_SECONDS));
            }
            return new Attempt(failures, Instant.EPOCH);
        });
    }

    private record Attempt(int failures, Instant blockedUntil) {}
}
