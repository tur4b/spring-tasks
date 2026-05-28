package org.example.service.api;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainer;

import java.util.List;

public interface TrainerService {

    UserCredentialsDTO createTrainer(TrainerCreateRequest trainerCreateRequest);

    TrainerProfileView updateTrainer(TrainerUpdateRequest trainerUpdateRequest);

    boolean existsById(Long trainerId);

    List<TrainerTrainingProfileView> findTrainingsOfTrainerByCriteria(TrainingsOfTrainerSearchCriteria searchCriteria);

    List<TraineeProfileTrainerDTO> findTrainersNotAssignedToTrainee(String traineeUsername);

    TrainerProfileView findTrainerViewByUsername(String trainerUsername);

    boolean deleteTrainer(String traineeUsername);

    Trainer findTrainerByUsername(String username);

    void updateStatus(UpdateStatusRequest statusRequest);
}
