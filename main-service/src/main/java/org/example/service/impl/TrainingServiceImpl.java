package org.example.service.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.WorkloadPublisher;
import org.example.dao.TrainingRepository;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Service layer for training operations
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Validated
@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainerTraineeRelationService trainerTraineeRelationService;
    private final WorkloadPublisher workloadPublisher;

    /**
     * Create a new training with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param createRequest the request object containing training details
     * @return created TrainingDTO with generated username and password
     */
    @Override
    public TrainingDTO createTraining(TrainingCreateRequest createRequest) {
        log.debug("Create Training request: {}", createRequest);

        boolean isTrainerTraineeRelation = trainerTraineeRelationService.existsTrainerTraineeRelation(
                createRequest.trainerUsername(),
                createRequest.traineeUsername()
        );

        if(!isTrainerTraineeRelation) {
            throw new NotFoundException(
                    "Trainer and Trainee relation does not exists",
                    ErrorResponse.ErrorPointer.id
            );
        }

        Training training = new Training();
        training.setName(createRequest.name());
        training.setDate(createRequest.date());
        training.setDuration(createRequest.duration());

        // set trainer
        Trainer trainer = trainerService.findTrainerByUsername(createRequest.trainerUsername());
        training.setTrainer(trainer);

        // set type
        TrainingType trainingType = trainer.getSpecialization();
        training.setType(trainingType);

        // set trainee
        Trainee trainee = traineeService.findTraineeByUsername(createRequest.traineeUsername());
        training.setTrainee(trainee);


        trainingRepository.save(training);
        log.info("Training Created: {}", training);
        workloadPublisher.publishAdd(training, trainer);

        return new TrainingDTO(
                training.getId(),
                trainee.getId(),
                trainer.getId(),
                training.getName(),
                trainingType.getId(),
                training.getDate(),
                training.getDuration()
        );
    }

    /**
     * Soft Delete a training by ID.
     *
     * @param trainingId the ID of the training to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTraining(Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new NotFoundException(
                        "Training not found by id: " + trainingId,
                        ErrorResponse.ErrorPointer.id
                ));

        if (!training.isActive()) {
            return false;
        }

        Trainer trainer = trainerService.findTrainerByUsername(training.getTrainer().getUser().getUsername());
        int affectedRows = trainingRepository.softDeleteById(trainingId);
        if (affectedRows > 0) {
            workloadPublisher.publishDelete(training, trainer);
        }
        return affectedRows > 0;
    }

}
