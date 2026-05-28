package org.example.service.api;

import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;

import java.util.List;

public interface TrainerTraineeRelationService {

    List<TraineeProfileTrainerDTO> updateTraineeTrainers(TraineeUpdateTrainersRequest updateTrainersRequest);

    boolean existsTrainerTraineeRelation(String trainerUsername, String traineeUsername);

    List<TraineeProfileTrainerDTO> findTrainersOfTraineeByTraineeUsername(String traineeUsername);

    List<TrainerProfileTraineeDTO> findTraineesOfTrainerByTrainerUsername(String trainerUsername);

}
