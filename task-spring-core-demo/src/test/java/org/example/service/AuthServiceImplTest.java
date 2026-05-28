package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.entity.User;
import org.example.exception.model.BadCredentialsException;
import org.example.service.api.PasswordEncoder;
import org.example.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── authenticate ────────────────────────────────────────────────────────

    @Test
    @DisplayName("authenticate - succeeds for valid credentials")
    void authenticate_ValidCredentials_Succeeds() {
        String rawPassword = "secret";
        String hashedPassword = "hashed";
        User user = buildUser("john.doe", hashedPassword);

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        assertThatCode(() -> authService.authenticate(new AuthRequest("john.doe", rawPassword)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("authenticate - throws BadCredentialsException when user does not exist")
    void authenticate_UserNotFound_ThrowsBadCredentialsException() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("missing.user", "secret")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("authenticate - throws BadCredentialsException when password is wrong")
    void authenticate_WrongPassword_ThrowsBadCredentialsException() {
        User user = buildUser("john.doe", "hashed");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("john.doe", "wrong")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // ─── changePassword ──────────────────────────────────────────────────────

    @Test
    @DisplayName("changePassword - updates password when old credentials are correct")
    void changePassword_ValidOldPassword_UpdatesPassword() {
        User user = buildUser("jane.doe", "oldHashed");
        ChangePasswordRequest req = new ChangePasswordRequest("jane.doe", "old", "newPass");

        when(userRepository.findByUsername("jane.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "oldHashed")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHashed");
        when(userRepository.changePassword("jane.doe", "newHashed")).thenReturn(1);

        assertThatCode(() -> authService.changePassword(req)).doesNotThrowAnyException();
        verify(userRepository).changePassword("jane.doe", "newHashed");
    }

    @Test
    @DisplayName("changePassword - throws BadCredentialsException when user not found")
    void changePassword_UserNotFound_ThrowsBadCredentialsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.changePassword(new ChangePasswordRequest("ghost", "old", "new")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("changePassword - throws BadCredentialsException when old password is wrong")
    void changePassword_WrongOldPassword_ThrowsBadCredentialsException() {
        User user = buildUser("jane.doe", "realHash");
        when(userRepository.findByUsername("jane.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "realHash")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.changePassword(new ChangePasswordRequest("jane.doe", "wrongOld", "new")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private User buildUser(String username, String password) {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);
        return user;
    }
}
