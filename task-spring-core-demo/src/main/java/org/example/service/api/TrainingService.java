package org.example.service.api;

import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;

public interface TrainingService {
    TrainingDTO createTraining(TrainingCreateRequest createRequest);
    boolean deleteTraining(Long trainingId);

}
