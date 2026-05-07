package org.example.service.api;

import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TrainingUpdateRequest;
import org.example.dto.response.TrainingDTO;

import java.util.List;

public interface TrainingService {
    List<TrainingDTO> findAll();
    TrainingDTO findTrainingById(Long traineeId);
    TrainingDTO createTraining(TrainingCreateRequest createRequest);
    TrainingDTO updateTraining(Long traineeId, TrainingUpdateRequest updateRequest);
    boolean deleteTraining(Long traineeId);
}
