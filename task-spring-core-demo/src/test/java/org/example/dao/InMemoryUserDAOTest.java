package org.example.dao;

import org.example.dao.impl.InMemoryUserDAO;
import org.example.entity.User;
import org.example.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryUserDAO Unit Tests")
class InMemoryUserDAOTest {

    @Mock
    private IdGenerator idGenerator;

    private Map<Long, User> userMap;
    private InMemoryUserDAO userDAO;

    private User buildUser(Long id, String firstName, String lastName, String username, boolean active) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(username);
        u.setPassword("pass1234");
        u.setActive(active);
        return u;
    }

    @BeforeEach
    void setUp() {
        userMap = new HashMap<>();
        userDAO = new InMemoryUserDAO(userMap, idGenerator);
    }

    @Test
    @DisplayName("findAll - returns only active users")
    void findAll_ReturnsOnlyActive() {
        userMap.put(1L, buildUser(1L, "John", "Doe", "john.doe", true));
        userMap.put(2L, buildUser(2L, "Jane", "Doe", "jane.doe", false)); // inactive
        userMap.put(3L, buildUser(3L, "Bob", "Smith", "bob.smith", true));

        List<User> result = userDAO.findAll();

        assertThat(result).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("john.doe", "bob.smith");
    }

    @Test
    @DisplayName("findById - returns user when id matches")
    void findById_Found() {
        Long id = 1L;
        userMap.put(1L, buildUser(id, "John", "Doe", "john.doe", true));

        Optional<User> result = userDAO.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("findById - returns empty Optional when id not found")
    void findById_NotFound() {
        assertThat(userDAO.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for null id")
    void findById_NullId() {
        assertThat(userDAO.findById(null)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for negative value id")
    void findById_NegativeValueId() {
        assertThat(userDAO.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAll - returns empty list when map is empty")
    void findAll_NotFound() {
        assertThat(userDAO.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - returns user when username matches")
    void findByUsername_Found() {
        userMap.put(1L, buildUser(1L, "John", "Doe", "john.doe", true));

        Optional<User> result = userDAO.findByUsername("john.doe");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("findByUsername - returns empty Optional when username not found")
    void findByUsername_NotFound() {
        assertThat(userDAO.findByUsername("unknown")).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - returns empty Optional for null username")
    void findByUsername_NullUsername() {
        assertThat(userDAO.findByUsername(null)).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - returns empty Optional for blank username")
    void findByUsername_BlankUsername() {
        assertThat(userDAO.findByUsername("  ")).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername - returns true when username exists")
    void existsByUsername_Found() {
        userMap.put(1L, buildUser(1L, "John", "Doe", "john.doe", true));

        assertThat(userDAO.existsByUsername("john.doe")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - returns false when username absent")
    void existsByUsername_NotFound() {
        assertThat(userDAO.existsByUsername("unknown")).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when user exists by ID")
    void existsById_Found() {
        Long id = 1L;
        userMap.put(id, buildUser(id, "John", "Doe", "john.doe", true));
        assertThat(userDAO.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false because user not found")
    void existsById_NotFound() {
        assertThat(userDAO.existsById(1L)).isFalse();
    }

    @Test
    @DisplayName("create - persists user and sets active flag")
    void create_Success() {
        when(idGenerator.getNextId("User")).thenReturn(50L);
        User user = buildUser(0L, "Alice", "Liddell", "alice.liddell", false);

        User result = userDAO.create(user);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.isActive()).isTrue();
        assertThat(userMap).containsKey(50L);
    }

    @Test
    @DisplayName("create - throws IllegalArgumentException for null user")
    void create_NullUser() {
        assertThatThrownBy(() -> userDAO.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    @DisplayName("update - updates existing user in the store")
    void update_Success() {
        User user = buildUser(1L, "John", "Doe", "john.doe", true);
        userMap.put(1L, user);

        user.setFirstName("Johnny");
        User result = userDAO.update(user);

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(userMap.get(1L).getFirstName()).isEqualTo("Johnny");
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for null user")
    void update_NullUser() {
        assertThatThrownBy(() -> userDAO.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for user with null ID")
    void update_NullId() {
        User user = new User();
        user.setId(null);

        assertThatThrownBy(() -> userDAO.update(user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById - returns true because user found")
    void deleteById_FoundUser() {
        userMap.put(1L, buildUser(1L, "John", "Doe", "john.doe", true));

        boolean result = userDAO.deleteById(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("deleteById - returns false because user not found")
    void deleteById_NotFoundUser() {
        boolean result = userDAO.deleteById(1L);
        assertThat(result).isFalse();
    }
}

