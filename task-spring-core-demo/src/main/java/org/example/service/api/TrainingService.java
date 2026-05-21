package org.example.service.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TrainingUpdateRequest;
import org.example.dto.response.TrainingDTO;

import java.util.List;

public interface TrainingService {

    List<TrainingView> findAllTrainingsView(@Valid AuthRequest authRequest);

    TrainingView findTrainingViewById(@NotNull(message = "Training id cant be null") Long trainingId,
                                      @Valid AuthRequest authRequest);

    TrainingDTO createTraining(@Valid TrainingCreateRequest createRequest,
                               @Valid AuthRequest authRequest);

    TrainingDTO updateTraining(@NotNull(message = "Training id cant be null")  Long trainingId,
                               @Valid TrainingUpdateRequest updateRequest,
                               @Valid AuthRequest authRequest);

    boolean deleteTraining(@NotNull(message = "Training id cant be null") Long trainingId,
                           @Valid AuthRequest authRequest);

}
