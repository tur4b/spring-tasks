package org.example.service;

import org.example.dao.TrainerRepository;
import org.example.dao.TrainingTypeRepository;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TrainerDTO;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.service.api.PasswordEncoder;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TrainerService - Service Slice Integration Tests")
class TrainerServiceImplIT extends AbstractServiceSliceTest {

    private static final AuthRequest AUTHENTICATED_USER = new AuthRequest("it.auth.user", "it-pass");
    private static final AuthRequest ANONYMOUS_USER = new AuthRequest("anonymous.user", "anonymous-pass");

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAuthenticatedUserExists() {
        userRepository.findByUsername(AUTHENTICATED_USER.username())
                .orElseGet(() -> userRepository.save(user(AUTHENTICATED_USER.username(), AUTHENTICATED_USER.password())));
    }

    @Test
    @DisplayName("createTrainer - persists trainer with linked user and specialization")
    void createTrainer_PersistsTrainerAndUser() {
        int typeId = savedType().getId();

        TrainerDTO dto = trainerService.createTrainer(
                new TrainerCreateRequest("Steve", "Coach", typeId));

        assertThat(dto).isNotNull();
        assertThat(trainerRepository.existsByUserUsername("steve.coach")).isTrue();
    }

    @Test
    @DisplayName("createTrainer - throws EntityNotFoundException for unknown specialization")
    void createTrainer_UnknownSpecialization_Throws() {
        assertThatThrownBy(() ->
                trainerService.createTrainer(new TrainerCreateRequest("X", "Y", 99999)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("existsById - true for persisted trainer")
    void existsById_TrueForPersistedTrainer() {
        int typeId = savedType().getId();
        TrainerDTO dto = trainerService.createTrainer(new TrainerCreateRequest("Ann", "Train", typeId));

        assertThat(trainerService.existsById(dto.id())).isTrue();
    }

    @Test
    @DisplayName("reassignTraineeToTrainers - links trainee to requested trainers and persists owning-side relation")
    void reassignTraineeToTrainers_ShouldPersistJoinTableLinks_ForRequestedTrainerIds() {
        int typeId = savedType().getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("Join", "Target", "Baku", LocalDate.of(1998, 8, 8)));
        TrainerDTO trainerOne = trainerService.createTrainer(new TrainerCreateRequest("Join", "One", typeId));
        TrainerDTO trainerTwo = trainerService.createTrainer(new TrainerCreateRequest("Join", "Two", typeId));

        trainerService.reassignTraineeToTrainers(trainee.id(), List.of(trainerOne.id(), trainerTwo.id()), AUTHENTICATED_USER);

        assertThat(trainerRepository.findAllByTraineesId(trainee.id()))
                .extracting(t -> t.getId())
                .containsExactlyInAnyOrder(trainerOne.id(), trainerTwo.id());
    }

    @Test
    @DisplayName("findTrainersNotAssignedToTrainee - excludes already assigned trainers and keeps unassigned ones")
    void findTrainersNotAssignedToTrainee_ShouldExcludeAssignedTrainers_WhenTraineeAlreadyHasAssignments() {
        int typeId = savedType().getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("Filter", "Target", "Baku", LocalDate.of(1997, 7, 7)));
        TrainerDTO assigned = trainerService.createTrainer(new TrainerCreateRequest("Assigned", "Coach", typeId));
        TrainerDTO free = trainerService.createTrainer(new TrainerCreateRequest("Free", "Coach", typeId));

        trainerService.reassignTraineeToTrainers(trainee.id(), List.of(assigned.id()), AUTHENTICATED_USER);

        assertThat(trainerService.findTrainersNotAssignedToTrainee("filter.target", AUTHENTICATED_USER))
                .extracting(TrainerView::getId)
                .contains(free.id())
                .doesNotContain(assigned.id());
    }

    @Test
    @DisplayName("findAllTrainersView - throws SecurityException when anonymous credentials are provided")
    void findAllTrainersView_ShouldThrowSecurityException_WhenAnonymousCredentialsAreUsed() {
        assertThatThrownBy(() -> trainerService.findAllTrainersView(ANONYMOUS_USER))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("INvalid credentials");
    }

    // helper methods

    private TrainingType savedType() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.STRENGTH);
        return trainingTypeRepository.save(type);
    }

    private org.example.entity.User user(String username, String password) {
        org.example.entity.User user = new org.example.entity.User();
        user.setFirstName("IT");
        user.setLastName("Auth");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}

