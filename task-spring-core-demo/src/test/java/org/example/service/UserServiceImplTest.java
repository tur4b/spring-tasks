package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("findById - throws EntityNotFoundException when user is missing")
    void findById_UserMissing_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L, new AuthRequest("u", "p")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id: 99");
    }

    @Test
    @DisplayName("createUser - uses serial username when base username already exists")
    void createUser_DuplicateUsername_UsesSerial() {
        UserCreateRequest request = new UserCreateRequest("John", "Doe");
        User mappedUser = new User();
        mappedUser.setFirstName("John");
        mappedUser.setLastName("Doe");

        UserDTO expectedDto = new UserDTO(1L, "John", "Doe", "john.doe1", null);

        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(userRepository.existsByUsername("john.doe")).thenReturn(true);
        when(userRepository.existsByUsername("john.doe1")).thenReturn(false);
        when(userMapper.toDTO(any(User.class))).thenReturn(expectedDto);

        UserDTO result = userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("john.doe1");
        assertThat(userCaptor.getValue().getPassword()).isNotBlank();
        assertThat(result.username()).isEqualTo("john.doe1");
    }

    @Test
    @DisplayName("createUser - throws IllegalArgumentException when create request is null")
    void createUser_ShouldThrowIllegalArgumentException_WhenCreateRequestIsNull() {
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserCreateRequest cannot be null");

        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - throws EntityNotFoundException when user id does not exist")
    void updateUser_ShouldThrowEntityNotFoundException_WhenUserIdDoesNotExist() {
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(77L, new UserUpdateRequest("First", "Last"), new AuthRequest("u", "p")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id: 77");

        verify(userRepository, never()).save(any());
    }
}

