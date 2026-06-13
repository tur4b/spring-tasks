package org.example.service;

import org.example.exception.model.AccountLockedException;
import org.example.service.impl.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoginAttemptService Unit Tests")
class LoginAttemptServiceTest {

    private static final String IP = "192.168.1.10";
    private static final String OTHER_IP = "10.0.0.1";

    @Test
    @DisplayName("validateNotBlocked passes when IP has no failed attempts")
    void validateNotBlocked_PassesWithNoFailedAttempts() {
        LoginAttemptService service = new LoginAttemptService();

        assertThatCode(() -> service.validateNotBlocked(IP))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotBlocked blocks IP after reaching max failed attempts")
    void validateNotBlocked_BlocksIpAfterMaxFailures() {
        LoginAttemptService service = new LoginAttemptService();

        service.onFailedLogin(IP);
        service.onFailedLogin(IP);
        service.onFailedLogin(IP);

        assertThatThrownBy(() -> service.validateNotBlocked(IP))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @DisplayName("validateNotBlocked does not block IP with fewer than max failed attempts")
    void validateNotBlocked_DoesNotBlockIpBeforeThreshold() {
        LoginAttemptService service = new LoginAttemptService();

        service.onFailedLogin(IP);
        service.onFailedLogin(IP);

        assertThatCode(() -> service.validateNotBlocked(IP))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotBlocked does not block a different IP")
    void validateNotBlocked_DoesNotBlockDifferentIp() {
        LoginAttemptService service = new LoginAttemptService();

        service.onFailedLogin(IP);
        service.onFailedLogin(IP);
        service.onFailedLogin(IP);

        assertThatCode(() -> service.validateNotBlocked(OTHER_IP))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onSuccessfulLogin clears blocked IP and allows future requests")
    void onSuccessfulLogin_ClearsIpBlock() {
        LoginAttemptService service = new LoginAttemptService();

        service.onFailedLogin(IP);
        service.onFailedLogin(IP);
        service.onFailedLogin(IP);

        service.onSuccessfulLogin(IP);

        assertThatCode(() -> service.validateNotBlocked(IP))
                .doesNotThrowAnyException();
    }
}
