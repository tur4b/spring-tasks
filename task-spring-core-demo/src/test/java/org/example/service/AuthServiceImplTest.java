package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.entity.User;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("authenticate - succeeds for valid credentials")
    void authenticate_ValidCredentials_Succeeds() {
        AuthRequest authRequest = new AuthRequest("john.doe", "secret");
        User user = buildUser("john.doe", "secret");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThatCode(() -> authService.authenticate(authRequest)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("authenticate - throws SecurityException when user does not exist")
    void authenticate_UserNotFound_ThrowsSecurityException() {
        AuthRequest authRequest = new AuthRequest("missing.user", "secret");

        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("INvalid credentials");
    }

    @Test
    @DisplayName("authenticate - throws SecurityException when password is invalid")
    void authenticate_InvalidPassword_ThrowsSecurityException() {
        AuthRequest authRequest = new AuthRequest("john.doe", "wrong-pass");
        User user = buildUser("john.doe", "secret");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("INvalid credentials");
    }

    private User buildUser(String username, String password) {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);
        return user;
    }
}

