package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dao.TraineeRepository;
import org.example.dto.request.*;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.mapper.TraineeMapper;
import org.example.service.api.UserService;
import org.example.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl Unit Tests")
class TraineeServiceImplTest {

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    @DisplayName("createTrainee - creates user, assigns it to trainee, and saves trainee")
    void createTrainee_Success() {
        TraineeCreateRequest request = new TraineeCreateRequest("John", "Doe", "Baku", LocalDate.of(2000, 1, 1));
        UserDTO createdUser = new UserDTO(10L, "John", "Doe", "john.doe", null);
        User userRef = new User();
        userRef.setId(10L);
        Trainee trainee = new Trainee();
        TraineeDTO expected = new TraineeDTO(1L, 10L, "Baku", true, LocalDate.of(2000, 1, 1), null, null);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);
        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(userService.getReferenceById(10L)).thenReturn(userRef);
        when(traineeMapper.toDTO(trainee, 10L)).thenReturn(expected);

        TraineeDTO result = traineeService.createTrainee(request);

        verify(traineeRepository).save(trainee);
        assertThat(trainee.getUser()).isEqualTo(userRef);
        assertThat(result.userId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("changePassword - throws when trainee username does not exist")
    void changePassword_UnknownTrainee_Throws() {
        ChangePasswordRequest request = new ChangePasswordRequest("missing.user", "new-pass");

        when(traineeRepository.existsByUserUsername("missing.user")).thenReturn(false);

        assertThatThrownBy(() -> traineeService.changePassword(request, new AuthRequest("admin", "admin")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found with username");

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("updateTrainee - throws EntityNotFoundException when trainee id is not found")
    void updateTrainee_ShouldThrowEntityNotFoundException_WhenTraineeIdDoesNotExist() {
        when(traineeRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee(
                55L,
                new TraineeUpdateRequest("Last", "First", "Address", LocalDate.of(1999, 1, 1)),
                new AuthRequest("u", "p")
        )).isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Trainee not found with ID: 55");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTraineeByUsername - returns false and does not call delete when username does not exist")
    void deleteTraineeByUsername_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        when(traineeRepository.findByUserUsername("unknown.user")).thenReturn(Optional.empty());

        boolean deleted = traineeService.deleteTraineeByUsername("unknown.user", new AuthRequest("u", "p"));

        assertThat(deleted).isFalse();
        verify(traineeRepository, never()).deleteById(anyLong());
    }
}

