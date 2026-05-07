package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TrainingDAO;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TrainingUpdateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Training;
import org.example.mapper.TrainingMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for training operations
 */
@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingMapper trainingMapper;
    private final TrainingDAO trainingDAO;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    /**
     * Constructor for <code>TrainingServiceImpl</code>
     * constructor injection applied for required dependencies
     *
     * @param trainingMapper   TrainingMapper instance
     * @param trainingDAO      TrainingDAO instance
     * @param traineeService   TraineeService instance
     * @param trainerService   TrainerService instance
     */
    public TrainingServiceImpl(TrainingMapper trainingMapper,
                               TrainingDAO trainingDAO,
                               TraineeService traineeService,
                               TrainerService trainerService) {
        this.trainingMapper = trainingMapper;
        this.trainingDAO = trainingDAO;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    /**
     * Get list of TrainingDTO
     *
     * @return list of trainings that converted to dtos
     */
    @Override
    public List<TrainingDTO> findAll() {
        log.debug("Find All Training");
        return trainingDAO.findAll()
                .stream()
                .map(trainingMapper::toDTO)
                .toList();
    }

    /**
     * Get TrainingDTO by training ID
     *
     * @param trainingId the ID of the training
     * @return TrainingDTO corresponding to the given ID
     */
    @Override
    public TrainingDTO findTrainingById(Long trainingId) {
        log.debug("Find Training by ID: {}", trainingId);
        return trainingDAO.findById(trainingId)
                .map(trainingMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Training not found with ID: " + trainingId));
    }

    /**
     * Create a new training with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param createRequest the request object containing training details
     * @return created TrainingDTO with generated username and password
     */
    @Override
    public TrainingDTO createTraining(TrainingCreateRequest createRequest) {
        if(createRequest == null) {
            log.error("TrainingCreateRequest cannot be null");
            throw new IllegalArgumentException("TrainingCreateRequest cannot be null");
        }

        log.debug("Create Training request: {}", createRequest);

        // check if trainee exists with given id
        if(!traineeService.existsById(createRequest.traineeId())) {
            throw new RuntimeException("TraineeId not found");
        }

        // check if trainer exists with given id
        if(!trainerService.existsById(createRequest.trainerId())) {
            throw new RuntimeException("TrainerId not found");
        }

        Training training = trainingMapper.toEntity(createRequest);

        Training createdTraining = trainingDAO.create(training);
        log.info("Training Created: {}", createdTraining);

        return trainingMapper.toDTO(createdTraining);
    }

    /**
     * Update an existing training profile.
     *
     * @param trainingId the ID of the training to update
     * @param updateRequest the request object containing updated training details
     * @return updated TrainingDTO
     */
    @Override
    public TrainingDTO updateTraining(Long trainingId, TrainingUpdateRequest updateRequest) {
        if(trainingId == null || updateRequest == null) {
            log.error("Training ID and TrainingUpdateRequest cannot be null");
            throw new IllegalArgumentException("TrainingId and TrainingUpdateRequest cannot be null");
        }

        log.debug("Update Training id: {} and request: {}", trainingId, updateRequest);

        // check if trainee exists with given id
        if(!traineeService.existsById(updateRequest.traineeId())) {
            throw new RuntimeException("TraineeId not found");
        }

        // check if trainer exists with given id
        if(!trainerService.existsById(updateRequest.trainerId())) {
            throw new RuntimeException("TrainerId not found");
        }

        // update the training details
        Training training = trainingDAO.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found with ID: " + trainingId));

        training.setTraineeId(updateRequest.traineeId());
        training.setTrainerId(updateRequest.trainerId());
        training.setName(updateRequest.name());
        training.setDate(updateRequest.date());
        training.setDuration(updateRequest.duration());
        training.setUpdatedAt(LocalDateTime.now());

        trainingDAO.update(training);

        return trainingMapper.toDTO(training);
    }

    /**
     * Soft Delete a training by ID.
     *
     * @param trainingId the ID of the training to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTraining(Long trainingId) {
        return trainingDAO.deleteById(trainingId);
    }

}
