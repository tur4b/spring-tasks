package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aspect.Secured;
import org.example.dao.TrainingRepository;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TrainingUpdateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.mapper.TrainingMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainingService;
import org.example.service.api.TrainingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for training operations
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Validated
@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingMapper trainingMapper;
    private final TrainingRepository trainingRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;

    /**
     * Get list of TrainingDTO
     *
     * @return list of trainings view
     */
    @Secured
    @Override
    public List<TrainingView> findAllTrainingsView(AuthRequest authRequest) {
        log.debug("Find All Training");
        return trainingRepository.findAllTrainingsView();
    }


    /**
     * Get TrainingView by trainingId
     *
     * @param trainingId the id of the training
     * @return TrainingView corresponding to the given trainingId
     */
    @Secured
    @Override
    public TrainingView findTrainingViewById(Long trainingId, AuthRequest authRequest) {
        log.debug("Find Training by ID: {}", trainingId);
        return trainingRepository.findTrainingViewById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training not found with ID: " + trainingId));
    }

    /**
     * Create a new training with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param createRequest the request object containing training details
     * @return created TrainingDTO with generated username and password
     */
    @Secured
    @Override
    public TrainingDTO createTraining(TrainingCreateRequest createRequest, AuthRequest authRequest) {
        log.debug("Create Training request: {}", createRequest);

        // check training type with given id
        if(!trainingTypeService.existsById(createRequest.typeId())) {
            throw new EntityNotFoundException("TraineeType not found");
        }

        // check if trainee exists with given id
        if(!traineeService.existsById(createRequest.traineeId())) {
            throw new EntityNotFoundException("Trainee not found");
        }

        // check if trainer exists with given id
        if(!trainerService.existsById(createRequest.trainerId())) {
            throw new EntityNotFoundException("Trainer not found");
        }

        boolean isTrainerTraineeRelation = trainerService.existsTrainerTraineeRelation(
                createRequest.trainerId(),
                createRequest.traineeId()
        );

        if(!isTrainerTraineeRelation) {
            throw new RuntimeException("Trainer and Trainee relation does not exists");
        }

        Training training = trainingMapper.toEntity(createRequest);

        // set type
        TrainingType trainingType = trainingTypeService.getReferenceById(createRequest.typeId());
        training.setType(trainingType);

        // set trainer
        Trainer trainerEntityRef = trainerService.getReferenceById(createRequest.trainerId());
        training.setTrainer(trainerEntityRef);

        // set trainee
        Trainee traineeEntityRef = traineeService.getReferenceById(createRequest.traineeId());
        training.setTrainee(traineeEntityRef);


        trainingRepository.save(training);
        log.info("Training Created: {}", training);

        return trainingMapper.toDTO(training, createRequest.traineeId(), createRequest.trainerId(), createRequest.typeId());
    }

    /**
     * Update an existing training profile.
     *
     * @param trainingId the ID of the training to update
     * @param updateRequest the request object containing updated training details
     * @return updated TrainingDTO
     */
    @Secured
    @Override
    public TrainingDTO updateTraining(Long trainingId, TrainingUpdateRequest updateRequest, AuthRequest authRequest) {
        log.debug("Update Training id: {} and request: {}", trainingId, updateRequest);

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found with ID: " + trainingId));

        // check if trainee exists with given id
        if(!traineeService.existsById(updateRequest.traineeId())) {
            throw new EntityNotFoundException("TraineeId not found");
        }

        // check if trainer exists with given id
        if(!trainerService.existsById(updateRequest.trainerId())) {
            throw new EntityNotFoundException("TrainerId not found");
        }

        // update the training details
        training.setName(updateRequest.name());
        training.setDate(updateRequest.date());
        training.setDuration(updateRequest.duration());

        // set type
        TrainingType trainingType = trainingTypeService.getReferenceById(updateRequest.typeId());
        training.setType(trainingType);

        // set trainer
        Trainer trainerEntityRef = trainerService.getReferenceById(updateRequest.trainerId());
        training.setTrainer(trainerEntityRef);

        // set trainee
        Trainee traineeEntityRef = traineeService.getReferenceById(updateRequest.traineeId());
        training.setTrainee(traineeEntityRef);

        trainingRepository.save(training);

        return trainingMapper.toDTO(training, updateRequest.traineeId(), updateRequest.trainerId(), updateRequest.typeId());
    }

    /**
     * Soft Delete a training by ID.
     *
     * @param trainingId the ID of the training to delete
     * @return true if deletion was successful, false otherwise
     */
    @Secured
    @Override
    public boolean deleteTraining(Long trainingId, AuthRequest authRequest) {
        return trainingRepository.softDeleteById(trainingId) != 0;
    }

}
