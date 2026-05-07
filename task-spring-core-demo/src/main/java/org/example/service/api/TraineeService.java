package org.example.service.api;

import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.response.TraineeDTO;

import java.util.List;

public interface TraineeService {
    List<TraineeDTO> findAll();
    TraineeDTO findTraineeById(Long traineeId);
    TraineeDTO createTrainee(TraineeCreateRequest traineeCreateRequest);
    TraineeDTO updateTrainee(Long traineeId, TraineeUpdateRequest traineeUpdateRequest);
    boolean deleteTrainee(Long traineeId);
    boolean existsById(Long id);
}
