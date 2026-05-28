package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.entity.User;
import org.example.exception.model.BadCredentialsException;
import org.example.service.api.AuthService;
import org.example.service.api.PasswordEncoder;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthService - Service Slice Integration Tests")
class AuthServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("authenticate - succeeds for valid credentials")
    void authenticate_IfValidCredentials_Succeeds() {
        userRepository.save(user("auth.user", "pass"));

        assertThatCode(() -> authService.authenticate(new AuthRequest("auth.user", "pass")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("authenticate - throws SecurityException for wrong password")
    void authenticate_IfWrongPassword_ThrowsSecurityException() {
        userRepository.save(user("auth.user2", "correct"));

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("auth.user2", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("authenticate - throws SecurityException for unknown username")
    void authenticate_IfUnknownUser_ThrowsSecurityException() {
        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("ghost.user", "pw")))
                .isInstanceOf(BadCredentialsException.class);
    }

    private User user(String username, String password) {
        User u = new User();
        u.setFirstName("Auth");
        u.setLastName("User");
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        return u;
    }
}
