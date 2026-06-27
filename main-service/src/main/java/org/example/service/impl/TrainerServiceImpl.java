package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.api.TrainingTypeService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service layer for trainer operations
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserService userService;
    private final TrainingTypeService trainingTypeService;
    private final TrainerTraineeRelationService trainerTraineeRelationService;
    private final TraineeRepository traineeRepository;

    /**
     * Get TrainerView by trainer ID
     *
     * @param trainerUsername the username of the trainer
     * @return TrainerView corresponding to the given ID
     */
    @Override
    public TrainerProfileView findTrainerViewByUsername(String trainerUsername) {
        log.debug("Find Trainer by username: {}", trainerUsername);

        TrainerDTO trainerDTO = trainerRepository.findTrainerDTOByUsername(trainerUsername)
                .orElseThrow(() -> new NotFoundException(
                        "Trainer not found with given username: " + trainerUsername,
                        ErrorResponse.ErrorPointer.username)
                );

        List<TrainerProfileTraineeDTO> trainerDTOS = trainerTraineeRelationService
                .findTraineesOfTrainerByTrainerUsername(trainerUsername);

        return new TrainerProfileView(
                trainerDTO.firstName(),
                trainerDTO.lastName(),
                trainerDTO.specializationId(),
                trainerDTO.isActive(),
                trainerDTOS
        );
    }

    /**
     * Delete a trainer by username.
     *
     * @param trainerUsername the username of the trainer to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTrainer(String trainerUsername) {
        if (!trainerRepository.existsByUserUsername(trainerUsername)) {
            log.debug("Trainer not found with username: {}", trainerUsername);
            return false;
        }

        trainerRepository.deleteByUserUsername(trainerUsername);
        return true;
    }

    /**
     * Register a new trainer with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     * Returns only credentials for security-focused registration response.
     *
     * @param trainerCreateRequest the request object containing trainer details
     * @return UserCredentialsDTO containing generated username and password
     * @throws NotFoundException if specialization does not exist
     */
    @Override
    public UserCredentialsDTO createTrainer(TrainerCreateRequest trainerCreateRequest) {
        log.debug("Register Trainer request: {}", trainerCreateRequest);

        // creating a new user with auto-generated credentials
        UserCredentialsDTO userCredentialsDTO = userService.createUser(
                new UserCreateRequest(
                    trainerCreateRequest.firstName(),
                    trainerCreateRequest.lastName()
                )
        );

        Trainer trainer = new Trainer();

        // set newly created user to trainer
        User createdUserEntityRef = userService.getReferenceById(userCredentialsDTO.id());
        trainer.setUser(createdUserEntityRef);

        // set specialization
        if (!trainingTypeService.existsById(trainerCreateRequest.specializationId())) {
            throw new NotFoundException("TrainingType not found with ID: " + trainerCreateRequest.specializationId(), ErrorResponse.ErrorPointer.id);
        }

        TrainingType specialization = trainingTypeService.getReferenceById(trainerCreateRequest.specializationId());
        trainer.setSpecialization(specialization);

        // save trainer with associated user and specialization
        trainerRepository.save(trainer);
        log.debug("Trainer registered with username: {}", userCredentialsDTO.username());

        // Best Practice: Return only credentials for security-sensitive registration
        return userCredentialsDTO;
    }

    /**
     * Update an existing trainer profile.
     *
     * @param updateRequest the request object containing updated trainer details
     * @return updated TrainerDTO
     */
    @Override
    public TrainerProfileView updateTrainer(TrainerUpdateRequest updateRequest) {
        log.debug("Update Trainer - request: {}", updateRequest);

        Trainer trainer = trainerRepository.findByUserUsername(updateRequest.username())
                .orElseThrow(() -> new NotFoundException(
                        "Trainer not found with username: " + updateRequest.username(),
                        ErrorResponse.ErrorPointer.username)
                );

        // update user details trainer belongs to
        User user = trainer.getUser();

        userService.updateUser(user.getId(),
                new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()));

        // set specialization
        if (!trainingTypeService.existsById(updateRequest.specializationId())) {
            throw new NotFoundException(
                    "TrainingType not found with ID: " + updateRequest.specializationId(),
                    ErrorResponse.ErrorPointer.id
            );
        }

        TrainingType specialization = trainingTypeService.getReferenceById(updateRequest.specializationId());
        trainer.setSpecialization(specialization);

        // save changes
        trainerRepository.save(trainer);

        List<TrainerProfileTraineeDTO> trainerDTOS = trainerTraineeRelationService
                .findTraineesOfTrainerByTrainerUsername(updateRequest.username());

        return new TrainerProfileView(
                user.getFirstName(),
                user.getLastName(),
                updateRequest.specializationId(),
                trainer.isActive(),
                trainerDTOS
        );
    }

    /**
     * Check if trainer exists by ID
     *
     * @param trainerId the id of trainer
     * @return true if trainer exists, false does not
     */
    @Override
    public boolean existsById(Long trainerId) {
        return trainerRepository.existsById(trainerId);
    }

    /**
     * Get all Trainings of Trainer according to given searchCriteria
     *
     * @param searchCriteria containing criteria data for filtering
     * @return list of TrainingView
     */
    @Transactional(readOnly = true)
    @Override
    public List<TrainerTrainingProfileView> findTrainingsOfTrainerByCriteria(TrainingsOfTrainerSearchCriteria searchCriteria) {
        return trainerRepository.findTrainingsOfTrainerByCriteria(
                searchCriteria.trainerUsername(),
                searchCriteria.fromDate(),
                searchCriteria.toDate(),
                searchCriteria.traineeName(),
                searchCriteria.typeId()
        );
    }

    /**
     * Get list of TrainerView which not assigned to given trainee
     *
     * @param traineeUsername username of Trainee
     * @return list of TraineeProfileTrainerDTO
     */
    @Transactional(readOnly = true)
    @Override
    public List<TraineeProfileTrainerDTO> findTrainersNotAssignedToTrainee(String traineeUsername) {
        if (!traineeRepository.existsByUserUsername(traineeUsername)) {
            return List.of();
        }
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }

    /**
     * Find the Trainer entity by username.
     *
     * @param username the trainer's login username
     * @return the Trainer entity
     * @throws org.example.exception.model.NotFoundException if no trainer with the given username exists
     */
    @Override
    public Trainer findTrainerByUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException(
                        "Trainer not found by username: " + username,
                        ErrorResponse.ErrorPointer.username
                ));
    }

    /**
     * Activate or deactivate the trainer identified in the request.
     *
     * @param statusRequest payload with the trainer's username and desired active state
     */
    @Override
    public void updateStatus(UpdateStatusRequest statusRequest) {
        if(statusRequest.active()) {
            activate(statusRequest.username());
        } else {
            deactivate(statusRequest.username());
        }
    }

    // helper methods

    /**
     * Activate Trainer with given username
     *
     * @param trainerUsername the username of the trainer
     * @throws NotFoundException if trainer not found
     * @throws RuntimeException if trainer is already active
     */
    private void activate(String trainerUsername) {
        Trainer trainer = findTrainerByUsername(trainerUsername);

        if(trainer.isActive()) {
            throw new RuntimeException("Trainer already active with username: " + trainerUsername);
        }
        trainer.setActive(true);
        trainerRepository.save(trainer);
    }

    /**
     * Deactivate Trainer with given ID
     *
     * @throws NotFoundException if trainer not found
     * @throws RuntimeException if trainer is already inactive
     */
    private void deactivate(String trainerUsername) {
        Trainer trainer = findTrainerByUsername(trainerUsername);

        if(!trainer.isActive()) {
            throw new RuntimeException("Trainer already inactive for username: " + trainerUsername);
        }
        trainer.setActive(false);
        trainerRepository.save(trainer);
    }
}
