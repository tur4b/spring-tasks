package org.example.dao;

import org.example.entity.User;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository - DAO Slice Integration Tests")
class UserRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("save + findByUsername - persists and retrieves by username")
    void saveAndFindByUsername() {
        userRepository.save(user("alice.bob", "secret"));

        assertThat(userRepository.findByUsername("alice.bob"))
                .isPresent()
                .get().extracting(User::getUsername).isEqualTo("alice.bob");
    }

    @Test
    @DisplayName("existsByUsername - returns true for existing, false for missing")
    void existsByUsername() {
        userRepository.save(user("exists.user", "pw"));

        assertThat(userRepository.existsByUsername("exists.user")).isTrue();
        assertThat(userRepository.existsByUsername("no.such")).isFalse();
    }

    @Test
    @DisplayName("findByUsername - returns empty optional when username is not present")
    void findByUsername_ShouldReturnEmptyOptional_WhenUsernameDoesNotExist() {
        assertThat(userRepository.findByUsername("missing.user")).isEmpty();
    }

    @Test
    @DisplayName("changePassword - updates password and returns 1 affected row")
    void changePassword_UpdatesSuccessfully() {
        userRepository.save(user("change.me", "old"));

        int rows = userRepository.changePassword("change.me", "new");

        assertThat(rows).isEqualTo(1);
        assertThat(userRepository.findByUsername("change.me"))
                .isPresent()
                .get().extracting(User::getPassword).isEqualTo("new");
    }

    @Test
    @DisplayName("changePassword - returns 0 for unknown username")
    void changePassword_UnknownUser_ReturnsZero() {
        assertThat(userRepository.changePassword("ghost.user", "pw")).isZero();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User user(String username, String password) {
        User u = new User();
        u.setFirstName("First");
        u.setLastName("Last");
        u.setUsername(username);
        u.setPassword(password);
        return u;
    }
}

