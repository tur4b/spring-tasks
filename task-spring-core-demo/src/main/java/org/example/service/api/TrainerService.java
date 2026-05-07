package org.example.service.api;

import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainerUpdateRequest;
import org.example.dto.response.TrainerDTO;

import java.util.List;

public interface TrainerService {
    List<TrainerDTO> findAll();
    TrainerDTO findTrainerById(Long trainerId);
    TrainerDTO createTrainer(TrainerCreateRequest trainerCreateRequest);
    TrainerDTO updateTrainer(Long trainerId, TrainerUpdateRequest trainerUpdateRequest);
    boolean deleteTrainer(Long trainerId);
    boolean existsById(Long trainerId);
}
