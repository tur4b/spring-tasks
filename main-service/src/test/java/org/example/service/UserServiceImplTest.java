package org.example.service;

import org.example.dao.UserRepository;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.mapper.UserMapper;
import org.example.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("findAll returns mapped user list")
    void findAll_ReturnsMappedList() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setCreatedAt(LocalDateTime.now());

        UserDTO dto = new UserDTO(1L, "John", "Doe", "john.doe", user.getCreatedAt());
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        assertThat(userService.findAll()).containsExactly(dto);
    }

    @Test
    @DisplayName("findByUsername returns mapped dto when user exists")
    void findByUsername_ReturnsMappedDto() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setCreatedAt(LocalDateTime.now());

        UserDTO dto = new UserDTO(1L, "John", "Doe", "john.doe", user.getCreatedAt());
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        assertThat(userService.findByUsername("john.doe")).isEqualTo(dto);
    }

    @Test
    @DisplayName("findById throws not found when user is missing")
    void findById_ThrowsWhenMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by id: 10");
    }

    @Test
    @DisplayName("createUser assigns generated credentials and persists the entity")
    void createUser_CreatesUser() {
        User entity = new User();
        entity.setFirstName("John");
        entity.setLastName("Doe");

        when(userMapper.toEntity(new UserCreateRequest("John", "Doe"))).thenReturn(entity);
        when(userRepository.existsByUsername("john.doe")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0, String.class));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var credentials = userService.createUser(new UserCreateRequest("John", "Doe"));

        assertThat(credentials.username()).isEqualTo("john.doe");
        assertThat(credentials.password()).isNotBlank();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("john.doe");
        assertThat(captor.getValue().getPassword()).startsWith("encoded-");
    }

    @Test
    @DisplayName("updateUser updates persisted user details")
    void updateUser_UpdatesUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDTO dto = new UserDTO(1L, "Johnny", "Smith", "john.doe", LocalDateTime.now());
        when(userMapper.toDTO(user)).thenReturn(dto);

        assertThat(userService.updateUser(1L, new UserUpdateRequest("Johnny", "Smith"))).isEqualTo(dto);
        verify(userRepository).save(user);
        assertThat(user.getFirstName()).isEqualTo("Johnny");
        assertThat(user.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("loadUserByUsername throws when user does not exist")
    void loadUserByUsername_ThrowsWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: missing");
    }
}

