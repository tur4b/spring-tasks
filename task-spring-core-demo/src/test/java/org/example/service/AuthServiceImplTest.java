package org.example.service;

import org.example.config.security.JwtService;
import org.example.config.security.LogoutTokenStore;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.entity.User;
import org.example.exception.model.BadCredentialsException;
import org.example.service.impl.AuthServiceImpl;
import org.example.service.impl.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private LogoutTokenStore logoutTokenStore;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("login returns JWT token for valid credentials")
    void login_ReturnsTokenForValidCredentials() {
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("john.doe")).thenReturn("jwt-token");

        String token = authService.login(new AuthRequest("john.doe", "secret"));

        org.assertj.core.api.Assertions.assertThat(token).isEqualTo("jwt-token");
        verify(loginAttemptService).validateNotBlocked("john.doe");
        verify(loginAttemptService).onSuccessfulLogin("john.doe");
        verify(jwtService).generateToken("john.doe");
    }

    @Test
    @DisplayName("authenticate records failed attempt when user is missing")
    void authenticate_ThrowsForMissingUser() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("john.doe", "secret")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(loginAttemptService).validateNotBlocked("john.doe");
        verify(loginAttemptService).onFailedLogin("john.doe");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("authenticate records failed attempt when password is invalid")
    void authenticate_ThrowsForInvalidPassword() {
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("john.doe", "secret")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(loginAttemptService).validateNotBlocked("john.doe");
        verify(loginAttemptService).onFailedLogin("john.doe");
        verify(loginAttemptService, never()).onSuccessfulLogin(eq("john.doe"));
    }

    @Test
    @DisplayName("logout invalidates a valid bearer token")
    void logout_InvalidatesToken() {
        when(jwtService.isTokenValid("jwt-token")).thenReturn(true);
        when(jwtService.extractExpiration("jwt-token")).thenReturn(Date.from(Instant.parse("2026-01-01T00:00:00Z")));

        authService.logout("Bearer jwt-token");

        verify(logoutTokenStore).invalidateToken(eq("jwt-token"), eq(Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    @DisplayName("changePassword updates the stored password when old password matches")
    void changePassword_UpdatesPassword() {
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("encoded-old");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-secret", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new");

        authService.changePassword(new ChangePasswordRequest("john.doe", "old-secret", "new-secret"));

        verify(userRepository).changePassword("john.doe", "encoded-new");
    }
}

