package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserCredentialsDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.api.UserService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserService - Service Slice Integration Tests")
class UserServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ─── findAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - returns all users in the database")
    void findAll_ReturnsAllUsers() {
        userRepository.save(buildUser("user1.exist", "First1", "Last1"));
        userRepository.save(buildUser("user2.exist", "First2", "Last2"));

        List<UserDTO> result = userService.findAll();

        assertThat(result).isNotEmpty().hasSizeGreaterThanOrEqualTo(2);
    }

    // ─── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById - returns UserDTO for existing user")
    void findById_ExistingUser_ReturnsDTO() {
        User saved = userRepository.save(buildUser("find.id.user", "John", "Doe"));

        UserDTO result = userService.findById(saved.getId());

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.username()).isEqualTo("find.id.user");
        assertThat(result.firstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("findById - throws NotFoundException for non-existent user")
    void findById_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.findById(99999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by id");
    }

    // ─── findByUsername ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUsername - returns UserDTO for existing username")
    void findByUsername_ExistingUsername_ReturnsDTO() {
        userRepository.save(buildUser("find.username", "Alice", "Smith"));

        UserDTO result = userService.findByUsername("find.username");

        assertThat(result.username()).isEqualTo("find.username");
        assertThat(result.firstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByUsername - throws NotFoundException for non-existent username")
    void findByUsername_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> userService.findByUsername("ghost.user"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by username");
    }

    // ─── createUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser - creates user with auto-generated credentials")
    void createUser_Success_ReturnsCredentials() {
        UserCreateRequest request = new UserCreateRequest("Create", "User");

        UserCredentialsDTO result = userService.createUser(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.username()).isNotBlank().contains("create").contains("user");
        assertThat(result.password()).isNotBlank();
        // Verify it was saved
        assertThat(userRepository.findById(result.id())).isPresent();
    }

    @Test
    @DisplayName("createUser - generates unique usernames for duplicates")
    void createUser_DuplicateName_GeneratesUniqueUsername() {
        userRepository.save(buildUser("john.doe", "John", "Doe"));

        UserCreateRequest request = new UserCreateRequest("John", "Doe");
        UserCredentialsDTO result = userService.createUser(request);

        assertThat(result.username()).isNotEqualTo("john.doe");
        assertThat(result.username()).startsWith("john.doe");
        assertThat(userRepository.findByUsername(result.username())).isPresent();
    }

    // ─── updateUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser - updates first and last name")
    void updateUser_ValidUpdate_UpdatesNames() {
        User saved = userRepository.save(buildUser("update.user", "Old", "Name"));

        UserDTO result = userService.updateUser(saved.getId(),
                new UserUpdateRequest("NewFirst", "NewLast"));

        assertThat(result.firstName()).isEqualTo("NewFirst");
        assertThat(result.lastName()).isEqualTo("NewLast");
        // Verify in database
        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(User::getFirstName)
                .isEqualTo("NewFirst");
    }

    @Test
    @DisplayName("updateUser - throws NotFoundException for non-existent user")
    void updateUser_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() ->
                userService.updateUser(99999L, new UserUpdateRequest("First", "Last")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by id");
    }

    // ─── existsById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("existsById - returns true for existing user")
    void existsById_ExistingUser_ReturnsTrue() {
        User saved = userRepository.save(buildUser("exists.check", "First", "Last"));

        assertThat(userService.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false for non-existent user")
    void existsById_NonExistent_ReturnsFalse() {
        assertThat(userService.existsById(99999L)).isFalse();
    }

    // ─── getReferenceById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getReferenceById - returns reference to existing user")
    void getReferenceById_ExistingUser_ReturnsReference() {
        User saved = userRepository.save(buildUser("ref.user", "First", "Last"));

        User result = userService.getReferenceById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private User buildUser(String username, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("encoded-password");
        return user;
    }
}

