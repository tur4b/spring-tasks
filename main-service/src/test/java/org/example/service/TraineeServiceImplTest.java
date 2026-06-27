package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.request.TrainingsOfTraineeSearchCriteria;
import org.example.dto.request.UpdateStatusRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TraineeProfileView;
import org.example.dto.response.TraineeTrainingProfileView;
import org.example.dto.response.UserCredentialsDTO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.api.UserService;
import org.example.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl Unit Tests")
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainerTraineeRelationService trainerTraineeRelationService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    @DisplayName("createTrainee persists trainee with generated credentials")
    void createTrainee_CreatesTrainee() {
        UserCredentialsDTO credentials = new UserCredentialsDTO(1L, "alice.smith", "raw-password");
        User user = new User();
        user.setId(1L);
        user.setUsername("alice.smith");
        when(userService.createUser(any())).thenReturn(credentials);
        when(userService.getReferenceById(1L)).thenReturn(user);
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = traineeService.createTrainee(new TraineeCreateRequest("Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10)));

        assertThat(result).isEqualTo(credentials);
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getAddress()).isEqualTo("Main street");
    }

    @Test
    @DisplayName("findTraineeViewByUsername returns profile with assigned trainers")
    void findTraineeViewByUsername_ReturnsProfile() {
        TraineeDTO traineeDTO = new TraineeDTO(1L, "Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10), true);
        List<TraineeProfileTrainerDTO> trainers = List.of(new TraineeProfileTrainerDTO("trainer.one", "John", "Doe", 1));
        when(traineeRepository.findTraineeDTOByUsername("alice.smith")).thenReturn(Optional.of(traineeDTO));
        when(trainerTraineeRelationService.findTrainersOfTraineeByTraineeUsername("alice.smith")).thenReturn(trainers);

        TraineeProfileView result = traineeService.findTraineeViewByUsername("alice.smith");

        assertThat(result.firstName()).isEqualTo("Alice");
        assertThat(result.trainers()).containsExactlyElementsOf(trainers);
    }

    @Test
    @DisplayName("updateTrainee updates user and trainee fields")
    void updateTrainee_UpdatesProfile() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setUsername("alice.smith");

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Old street");
        trainee.setDateOfBirth(LocalDate.of(1995, 1, 10));
        trainee.setActive(true);

        when(traineeRepository.findByUserUsername("alice.smith")).thenReturn(Optional.of(trainee));
        when(userService.updateUser(1L, new UserUpdateRequest("Alicia", "Johnson"))).thenReturn(null);
        when(trainerTraineeRelationService.findTrainersOfTraineeByTraineeUsername("alice.smith")).thenReturn(List.of());
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TraineeProfileView result = traineeService.updateTrainee(new TraineeUpdateRequest("alice.smith", "Alicia", "Johnson", "New street", LocalDate.of(1995, 1, 10)));

        assertThat(result.firstName()).isEqualTo("Alice");
        assertThat(trainee.getAddress()).isEqualTo("New street");
        verify(userService).updateUser(1L, new UserUpdateRequest("Alicia", "Johnson"));
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria delegates to repository")
    void findTrainingsOfTraineeByCriteria_DelegatesToRepository() {
        List<TraineeTrainingProfileView> trainings = List.of(new TraineeTrainingProfileView("Strength", LocalDate.of(2026, 1, 1), 1, 45, "Trainer One"));
        when(traineeRepository.findTrainingsOfTraineeByCriteria("alice.smith", null, null, null, null)).thenReturn(trainings);

        assertThat(traineeService.findTrainingsOfTraineeByCriteria(new TrainingsOfTraineeSearchCriteria("alice.smith", null, null, null, null)))
                .isEqualTo(trainings);
    }

    @Test
    @DisplayName("updateStatus deactivates an active trainee")
    void updateStatus_DeactivatesTrainee() {
        User user = new User();
        user.setUsername("alice.smith");
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setActive(true);
        when(traineeRepository.findByUserUsername("alice.smith")).thenReturn(Optional.of(trainee));

        traineeService.updateStatus(new UpdateStatusRequest("alice.smith", false));

        assertThat(trainee.isActive()).isFalse();
        verify(traineeRepository).save(trainee);
    }
}

