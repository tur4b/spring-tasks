package org.example.service.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.dao.projection.TraineeView;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.*;
import org.example.dto.response.TraineeDTO;
import org.example.entity.Trainee;

import java.util.List;

public interface TraineeService {

    List<TraineeView> findAllTraineesView(@Valid AuthRequest authRequest);

    TraineeView findTraineeViewById(@NotNull(message = "Trainee id can't be null") Long traineeId,
                                    @Valid AuthRequest authRequest);

    TraineeDTO createTrainee(@Valid TraineeCreateRequest traineeCreateRequest);

    TraineeDTO updateTrainee(@NotNull(message = "Trainee id can't be null") Long traineeId,
                             @Valid TraineeUpdateRequest traineeUpdateRequest,
                             @Valid AuthRequest authRequest);

    boolean deleteTraineeByUsername(@NotBlank(message = "Username can't be blank") String username,
                                    @Valid AuthRequest authRequest);

    boolean existsById(@NotNull(message = "Trainee id can't be null") Long id);

    void activate(@NotNull(message = "Trainee id can't be null") Long traineeId,
                  @Valid AuthRequest authRequest);

    void deactivate(@NotNull(message = "Trainee id can't be null") Long traineeId,
                    @Valid AuthRequest authRequest);

    Trainee getReferenceById(@NotNull(message = "Trainee id can't be null") Long traineeId);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest,
                        @Valid AuthRequest authRequest);

    List<TrainingView> findTrainingsOfTraineeByCriteria(@Valid TrainingsOfTraineeSearchCriteria searchCriteria,
                                                        @Valid AuthRequest authRequest);

}
