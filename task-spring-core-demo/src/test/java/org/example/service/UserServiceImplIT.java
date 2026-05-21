package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.service.api.UserService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import jakarta.persistence.EntityNotFoundException;

@DisplayName("UserService - Service Slice Integration Tests")
class UserServiceImplIT extends AbstractServiceSliceTest {

    private static final AuthRequest AUTHENTICATED_USER = new AuthRequest("it.auth.user", "it-pass");
    private static final AuthRequest ANONYMOUS_USER = new AuthRequest("anonymous.user", "anonymous-pass");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void ensureAuthenticatedUserExists() {
        userRepository.findByUsername(AUTHENTICATED_USER.username())
                .orElseGet(() -> userRepository.save(user(AUTHENTICATED_USER.username(), AUTHENTICATED_USER.password())));
    }

    @Test
    @DisplayName("createUser - persists user with auto-generated username + password")
    void createUser_PersistsWithGeneratedCredentials() {
        UserDTO dto = userService.createUser(new UserCreateRequest("Alice", "Stone"));

        assertThat(dto.username()).isEqualTo("alice.stone");
        assertThat(userRepository.findByUsername("alice.stone")).isPresent();
        assertThat(userRepository.findByUsername("alice.stone").orElseThrow().getPassword()).isNotBlank();
    }

    @Test
    @DisplayName("createUser - appends serial suffix on duplicate username")
    void createUser_SerialSuffixOnDuplicate() {
        userService.createUser(new UserCreateRequest("Bob", "Smith"));
        UserDTO second = userService.createUser(new UserCreateRequest("Bob", "Smith"));

        assertThat(second.username()).isEqualTo("bob.smith1");
    }

    @Test
    @DisplayName("findById - throws EntityNotFoundException for missing id")
    void findById_Missing_Throws() {
        assertThatThrownBy(() -> userService.findById(99999L, AUTHENTICATED_USER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("existsById - true/false for present/absent user")
    void existsById() {
        User saved = userRepository.save(user("e.user", "pw"));

        assertThat(userService.existsById(saved.getId())).isTrue();
        assertThat(userService.existsById(99999L)).isFalse();
    }

    @Test
    @DisplayName("findByUsername - throws EntityNotFoundException when username does not exist")
    void findByUsername_ShouldThrowEntityNotFoundException_WhenUsernameDoesNotExist() {
        assertThatThrownBy(() -> userService.findByUsername("ghost.user", AUTHENTICATED_USER))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by username: ghost.user");
    }

    @Test
    @DisplayName("changePassword - updates existing user's password through repository modifying query")
    void changePassword_ShouldPersistNewPassword_WhenUsernameExists() {
        userRepository.save(user("update.pass", "old-pass"));

        userService.changePassword(new ChangePasswordRequest("update.pass", "new-pass"), AUTHENTICATED_USER);

        assertThat(userRepository.findByUsername("update.pass"))
                .isPresent()
                .get()
                .extracting(User::getPassword)
                .isEqualTo("new-pass");
    }

    @Test
    @DisplayName("findAll - throws SecurityException when anonymous credentials are provided")
    void findAll_ShouldThrowSecurityException_WhenAnonymousCredentialsAreUsed() {
        assertThatThrownBy(() -> userService.findAll(ANONYMOUS_USER))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("INvalid credentials");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User user(String username, String password) {
        User u = new User();
        u.setFirstName("E");
        u.setLastName("User");
        u.setUsername(username);
        u.setPassword(password);
        return u;
    }
}

