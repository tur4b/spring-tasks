package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dao.TrainerRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainingTypeService;
import org.example.service.api.UserService;
import org.example.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl Unit Tests")
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    @DisplayName("createTrainer - throws when specialization does not exist")
    void createTrainer_MissingSpecialization_Throws() {
        TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Coach", 99);
        when(userService.createUser(any())).thenReturn(new UserDTO(1L, "Jane", "Coach", "jane.coach", null));
        when(trainingTypeService.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TrainingType not found");
    }

    @Test
    @DisplayName("reassignTraineeToTrainers - throws when any trainer id is missing")
    void reassignTraineeToTrainers_MissingTrainer_Throws() {
        when(traineeService.getReferenceById(1L)).thenReturn(new Trainee());
        when(trainerRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new Trainer()));

        assertThatThrownBy(() -> trainerService.reassignTraineeToTrainers(1L, List.of(1L, 2L), new AuthRequest("u", "p")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("All trainers not found");

        verify(trainerRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("changePassword - throws EntityNotFoundException and skips userService call when trainer username is unknown")
    void changePassword_ShouldThrowEntityNotFoundException_WhenTrainerUsernameDoesNotExist() {
        ChangePasswordRequest request = new ChangePasswordRequest("ghost.trainer", "new-pass");
        AuthRequest authRequest = new AuthRequest("admin", "admin");

        when(trainerRepository.existsByUserUsername("ghost.trainer")).thenReturn(false);

        assertThatThrownBy(() -> trainerService.changePassword(request, authRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found with username: ghost.trainer");

        verify(userService, never()).changePassword(any(), any());
    }
}

