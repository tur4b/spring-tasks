package org.example.service.api;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainee;

import java.util.List;

public interface TraineeService {

    TraineeProfileView findTraineeViewByUsername(String username);
    UserCredentialsDTO createTrainee(TraineeCreateRequest traineeCreateRequest);
    TraineeProfileView updateTrainee(TraineeUpdateRequest traineeUpdateRequest);
    boolean deleteTrainee(String username);
    boolean existsById(Long id);
    Trainee getReferenceById(Long traineeId);
    List<TraineeTrainingProfileView> findTrainingsOfTraineeByCriteria(TrainingsOfTraineeSearchCriteria searchCriteria);
    boolean existsByUsername(String username);
    Trainee findTraineeByUsername(String username);
    void updateStatus(UpdateStatusRequest statusRequest);

}
