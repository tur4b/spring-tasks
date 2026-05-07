package org.example.service;

import org.example.dao.TraineeDAO;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.mapper.TraineeMapper;
import org.example.service.api.UserService;
import org.example.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl Unit Tests")
class TraineeServiceImplTest {

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private UserService userService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private TraineeDTO traineeDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUserId(10L);
        trainee.setAddress("123 Main St");
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 10));

        traineeDTO = new TraineeDTO(1L, 10L, "123 Main St", LocalDate.of(1995, 5, 10), LocalDateTime.now());
        userDTO = new UserDTO(10L, "John", "Doe", "john.doe", LocalDateTime.now());
    }

    @Test
    @DisplayName("findAll - returns list of TraineeDTOs")
    void findAll_ReturnsMappedDTOs() {
        when(traineeDAO.findAll()).thenReturn(List.of(trainee));
        when(traineeMapper.toDTO(trainee)).thenReturn(traineeDTO);

        List<TraineeDTO> result = traineeService.findAll();

        assertThat(result).hasSize(1).containsExactly(traineeDTO);
        verify(traineeDAO).findAll();
    }

    @Test
    @DisplayName("findAll - returns empty list when no trainees exist")
    void findAll_EmptyList() {
        when(traineeDAO.findAll()).thenReturn(List.of());

        assertThat(traineeService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findTraineeById - returns TraineeDTO when found")
    void findTraineeById_Found_ReturnsDTO() {
        when(traineeDAO.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toDTO(trainee)).thenReturn(traineeDTO);

        TraineeDTO result = traineeService.findTraineeById(1L);

        assertThat(result).isEqualTo(traineeDTO);
    }

    @Test
    @DisplayName("findTraineeById - throws RuntimeException when not found")
    void findTraineeById_NotFound_ThrowsException() {
        when(traineeDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findTraineeById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found with ID: 99");
    }

    @Test
    @DisplayName("createTrainee - creates trainee and returns TraineeDTO")
    void createTrainee_ValidRequest_ReturnsDTO() {
        TraineeCreateRequest request = new TraineeCreateRequest("Doe", "John", "123 Main St", LocalDate.of(1995, 5, 10));

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userDTO);
        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeDAO.create(trainee)).thenReturn(trainee);
        when(traineeMapper.toDTO(trainee)).thenReturn(traineeDTO);

        TraineeDTO result = traineeService.createTrainee(request);

        assertThat(result).isEqualTo(traineeDTO);
        verify(userService).createUser(any(UserCreateRequest.class));
        verify(traineeDAO).create(trainee);
    }

    @Test
    @DisplayName("createTrainee - throws IllegalArgumentException for null request")
    void createTrainee_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> traineeService.createTrainee(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TraineeCreateRequest cannot be null");
    }

    @Test
    @DisplayName("updateTrainee - updates trainee and returns updated TraineeDTO")
    void updateTrainee_ValidRequest_ReturnsUpdatedDTO() {
        TraineeUpdateRequest request = new TraineeUpdateRequest("Doe", "John", "456 Other St", LocalDate.of(1995, 5, 10));

        when(traineeDAO.findById(1L)).thenReturn(Optional.of(trainee));
        when(userService.findUserById(trainee.getUserId())).thenReturn(userDTO);
        when(userService.updateUser(eq(userDTO.id()), any(UserUpdateRequest.class))).thenReturn(userDTO);
        when(traineeMapper.toDTO(trainee)).thenReturn(traineeDTO);

        TraineeDTO result = traineeService.updateTrainee(1L, request);

        assertThat(result).isEqualTo(traineeDTO);
        verify(traineeDAO).update(trainee);
    }

    @Test
    @DisplayName("updateTrainee - throws IllegalArgumentException for null traineeId")
    void updateTrainee_NullId_ThrowsException() {
        assertThatThrownBy(() -> traineeService.updateTrainee(null, new TraineeUpdateRequest("A", "B", "addr", LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTrainee - throws IllegalArgumentException for null request")
    void updateTrainee_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> traineeService.updateTrainee(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTrainee - throws RuntimeException when trainee not found")
    void updateTrainee_NotFound_ThrowsException() {
        when(traineeDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee(99L, new TraineeUpdateRequest("A", "B", "addr", LocalDate.now())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found with ID: 99");
    }

    @Test
    @DisplayName("deleteTrainee - returns true when deletion succeeds")
    void deleteTrainee_ReturnsTrue() {
        when(traineeDAO.deleteById(1L)).thenReturn(true);

        assertThat(traineeService.deleteTrainee(1L)).isTrue();
        verify(traineeDAO).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTrainee - returns false when trainee not found")
    void deleteTrainee_ReturnsFalse() {
        when(traineeDAO.deleteById(99L)).thenReturn(false);

        assertThat(traineeService.deleteTrainee(99L)).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when trainee exists")
    void existsById_ReturnsTrue() {
        when(traineeDAO.existsById(1L)).thenReturn(true);

        assertThat(traineeService.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when trainee does not exist")
    void existsById_ReturnsFalse() {
        when(traineeDAO.existsById(99L)).thenReturn(false);

        assertThat(traineeService.existsById(99L)).isFalse();
    }
}

