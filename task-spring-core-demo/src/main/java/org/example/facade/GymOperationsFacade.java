package org.example.facade;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainingService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Facade class that provides a unified interface to the Gym services.
 * Delegates all operations to the appropriate service beans.
 */
@Slf4j
@Component
public class GymOperationsFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    /**
     * Constructor-based injection of all service dependencies.
     *
     * @param traineeService  TraineeService instance
     * @param trainerService  TrainerService instance
     * @param trainingService TrainingService instance
     */
    public GymOperationsFacade(TraineeService traineeService,
                               TrainerService trainerService,
                               TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Trainee --------------------

    public List<TraineeDTO> findAllTrainees() {
        log.info("Facade: findAllTrainees");
        return traineeService.findAll();
    }

    public TraineeDTO findTraineeById(Long traineeId) {
        log.info("Facade: findTraineeById({})", traineeId);
        return traineeService.findTraineeById(traineeId);
    }

    public TraineeDTO createTrainee(TraineeCreateRequest request) {
        log.info("Facade: createTrainee");
        return traineeService.createTrainee(request);
    }

    public TraineeDTO updateTrainee(Long traineeId, TraineeUpdateRequest request) {
        log.info("Facade: updateTrainee({})", traineeId);
        return traineeService.updateTrainee(traineeId, request);
    }

    public boolean deleteTrainee(Long traineeId) {
        log.info("Facade: deleteTrainee({})", traineeId);
        return traineeService.deleteTrainee(traineeId);
    }

    // Trainer --------------------

    public List<TrainerDTO> findAllTrainers() {
        log.info("Facade: findAllTrainers");
        return trainerService.findAll();
    }

    public TrainerDTO findTrainerById(Long trainerId) {
        log.info("Facade: findTrainerById({})", trainerId);
        return trainerService.findTrainerById(trainerId);
    }

    public TrainerDTO createTrainer(TrainerCreateRequest request) {
        log.info("Facade: createTrainer");
        return trainerService.createTrainer(request);
    }

    public TrainerDTO updateTrainer(Long trainerId, TrainerUpdateRequest request) {
        log.info("Facade: updateTrainer({})", trainerId);
        return trainerService.updateTrainer(trainerId, request);
    }

    public boolean deleteTrainer(Long trainerId) {
        log.info("Facade: deleteTrainer({})", trainerId);
        return trainerService.deleteTrainer(trainerId);
    }

    // Training --------------------

    public List<TrainingDTO> findAllTrainings() {
        log.info("Facade: findAllTrainings");
        return trainingService.findAll();
    }

    public TrainingDTO findTrainingById(Long trainingId) {
        log.info("Facade: findTrainingById({})", trainingId);
        return trainingService.findTrainingById(trainingId);
    }

    public TrainingDTO createTraining(TrainingCreateRequest request) {
        log.info("Facade: createTraining");
        return trainingService.createTraining(request);
    }

    public TrainingDTO updateTraining(Long trainingId, TrainingUpdateRequest request) {
        log.info("Facade: updateTraining({})", trainingId);
        return trainingService.updateTraining(trainingId, request);
    }

    public boolean deleteTraining(Long trainingId) {
        log.info("Facade: deleteTraining({})", trainingId);
        return trainingService.deleteTraining(trainingId);
    }

}

