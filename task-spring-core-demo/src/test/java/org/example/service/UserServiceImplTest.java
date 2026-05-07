package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.impl.UserServiceImpl;
import org.example.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password123");

        userDTO = new UserDTO(1L, "John", "Doe", "john.doe", LocalDateTime.now());
    }

    @Test
    @DisplayName("findAll - returns list of UserDTOs")
    void findAll_ReturnsMappedDTOs() {
        when(userDAO.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        List<UserDTO> result = userService.findAll();

        assertThat(result).hasSize(1).containsExactly(userDTO);
        verify(userDAO).findAll();
        verify(userMapper).toDTO(user);
    }

    @Test
    @DisplayName("findAll - returns empty list when no users exist")
    void findAll_EmptyList() {
        when(userDAO.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findUserById - returns UserDTO when user found")
    void findUserById_UserFound_ReturnsDTO() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.findUserById(1L);

        assertThat(result).isEqualTo(userDTO);
        verify(userDAO).findById(1L);
    }

    @Test
    @DisplayName("findUserById - throws RuntimeException when user not found")
    void findUserById_UserNotFound_ThrowsException() {
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found by id: 99");
    }

    @Test
    @DisplayName("findUserByUsername - returns UserDTO when found")
    void findUserByUsername_Found_ReturnsDTO() {
        when(userDAO.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.findUserByUsername("john.doe");

        assertThat(result).isEqualTo(userDTO);
    }

    @Test
    @DisplayName("findUserByUsername - throws RuntimeException when not found")
    void findUserByUsername_NotFound_ThrowsException() {
        when(userDAO.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found by username: unknown");
    }

    @Test
    @DisplayName("createUser - creates user and returns UserDTO")
    void createUser_ValidRequest_ReturnsDTO() {
        UserCreateRequest request = new UserCreateRequest("John", "Doe");

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userDAO.existsByUsername(anyString())).thenReturn(false);
        when(idGenerator.getNextId(anyString())).thenReturn(1L);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUser(request);

        assertThat(result).isEqualTo(userDTO);
        verify(userDAO).create(user);
    }

    @Test
    @DisplayName("createUser - appends serial number when username already exists")
    void createUser_DuplicateUsername_AppendsSerial() {
        UserCreateRequest request = new UserCreateRequest("John", "Doe");

        when(userMapper.toEntity(request)).thenReturn(user);
        // first check: username exists; second check: does not
        when(userDAO.existsByUsername(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        when(idGenerator.getNextId(anyString())).thenReturn(2L);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUser(request);

        assertThat(result).isNotNull();
        verify(userDAO, times(2)).existsByUsername(anyString());
    }

    @Test
    @DisplayName("createUser - throws IllegalArgumentException for null request")
    void createUser_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserCreateRequest cannot be null");
    }

    @Test
    @DisplayName("updateUser - updates user and returns updated UserDTO")
    void updateUser_ValidRequest_ReturnsUpdatedDTO() {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Doe");
        UserDTO updatedDTO = new UserDTO(1L, "Jane", "Doe", "jane.doe", LocalDateTime.now());

        when(userDAO.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(updatedDTO);

        UserDTO result = userService.updateUser(1L, request);

        assertThat(result).isEqualTo(updatedDTO);
        verify(userDAO).update(user);
    }

    @Test
    @DisplayName("updateUser - throws IllegalArgumentException for null userId")
    void updateUser_NullId_ThrowsException() {
        assertThatThrownBy(() -> userService.updateUser(null, new UserUpdateRequest("A", "B")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateUser - throws IllegalArgumentException for null request")
    void updateUser_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> userService.updateUser(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateUser - throws RuntimeException when user not found")
    void updateUser_UserNotFound_ThrowsException() {
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UserUpdateRequest("A", "B")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found by id: 99");
    }

    @Test
    @DisplayName("deleteUser - returns true when deletion succeeds")
    void deleteUser_ReturnsTrue() {
        when(userDAO.deleteById(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertThat(result).isTrue();
        verify(userDAO).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - returns false when user does not exist")
    void deleteUser_ReturnsFalse() {
        when(userDAO.deleteById(99L)).thenReturn(false);

        assertThat(userService.deleteUser(99L)).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when user exists")
    void existsById_ReturnsTrue() {
        when(userDAO.existsById(1L)).thenReturn(true);

        assertThat(userService.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when user does not exist")
    void existsById_ReturnsFalse() {
        when(userDAO.existsById(99L)).thenReturn(false);

        assertThat(userService.existsById(99L)).isFalse();
    }
}

