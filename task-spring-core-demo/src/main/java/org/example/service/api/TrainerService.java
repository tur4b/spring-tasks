package org.example.service.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.dao.projection.TrainerView;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.*;
import org.example.dto.response.TrainerDTO;
import org.example.entity.Trainer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainerService {

    List<TrainerView> findAllTrainersView(@Valid AuthRequest authRequest);

    TrainerView findTrainerViewById(@NotNull(message = "Trainer id can't be null") Long trainerId,
                                    @Valid AuthRequest authRequest);

    TrainerDTO createTrainer(@Valid TrainerCreateRequest trainerCreateRequest);

    TrainerDTO updateTrainer(@NotNull(message = "Trainer id can't be null") Long trainerId,
                             @Valid TrainerUpdateRequest trainerUpdateRequest,
                             @Valid AuthRequest authRequest);


    boolean existsById(@NotNull(message = "Trainer id can't be null") Long trainerId);

    void activate(@NotNull(message = "Trainer id can't be null") Long trainerId,
                  @Valid AuthRequest authRequest);

    void deactivate(@NotNull(message = "Trainer id can't be null") Long trainerId,
                    @Valid AuthRequest authRequest);

    boolean existsTrainerTraineeRelation(@NotNull(message = "Trainer id can't be null") Long trainerId,
                                         @NotNull(message = "Trainee id can't be null") Long traineeId);

    Trainer getReferenceById(@NotNull(message = "Trainer id can't be null") Long trainerId);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest,
                        @Valid AuthRequest authRequest);

    List<TrainingView> findTrainingsOfTrainerByCriteria(@Valid TrainingsOfTrainerSearchCriteria searchCriteria,
                                                        @Valid AuthRequest authRequest);

    List<TrainerView> findTrainersNotAssignedToTrainee(@NotBlank(message = "Trainee username can't be blank") String traineeUsername,
                                                       @Valid AuthRequest authRequest);


    void reassignTraineeToTrainers(@NotNull(message = "Trainee id can't be null") Long traineeId,
                                   @NotNull(message = "TrainerIds can't be null") List<Long> trainerIds,
                                   @Valid AuthRequest authRequest);

}
