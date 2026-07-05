package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.WorkloadPublisher;
import org.example.dao.TraineeRepository;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainee;
import org.example.entity.Training;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service layer for trainee operations
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
@Service
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final UserService userService;
    private final TrainerTraineeRelationService trainerTraineeRelationService;
    private final WorkloadPublisher workloadPublisher;

    /**
     * Get Trainee Profile by trainee username
     *
     * @param traineeUsername the username of the trainee
     * @throws NotFoundException if Trainee not found with given username
     * @return TraineeView corresponding to the given ID
     */
    @Transactional(readOnly = true)
    @Override
    public TraineeProfileView findTraineeViewByUsername(String traineeUsername) {
        log.debug("Find Trainee by username: {}", traineeUsername);

        TraineeDTO traineeDTO = traineeRepository.findTraineeDTOByUsername(traineeUsername)
                .orElseThrow(() -> new NotFoundException(
                        "Trainee not found with given username: " + traineeUsername,
                        ErrorResponse.ErrorPointer.username)
                );

        List<TraineeProfileTrainerDTO> trainerDTOS = trainerTraineeRelationService.findTrainersOfTraineeByTraineeUsername(traineeUsername);

        return new TraineeProfileView(
                traineeDTO.firstName(),
                traineeDTO.lastName(),
                traineeDTO.address(),
                traineeDTO.dateOfBirth(),
                traineeDTO.isActive(),
                trainerDTOS
        );
    }

    /**
     * Create a new trainee with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param createRequest the request object containing trainee details
     * @return created UserCredentialsDTO with generated username and password
     */
    @Override
    public UserCredentialsDTO createTrainee(TraineeCreateRequest createRequest) {
        log.debug("Create Trainee request: {}", createRequest);

        // creating a new user
        UserCredentialsDTO userCredentialsDTO = userService.createUser(new UserCreateRequest(createRequest.firstName(), createRequest.lastName()));

        // create a new trainee and set required fields
        Trainee trainee = new Trainee();
        trainee.setAddress(createRequest.address());
        trainee.setDateOfBirth(createRequest.dateOfBirth());

        User userEntityRef = userService.getReferenceById(userCredentialsDTO.id());
        trainee.setUser(userEntityRef);

        traineeRepository.save(trainee);
        log.debug("Trainee created with username: {}", userCredentialsDTO.username());

        return userCredentialsDTO;
    }

    /**
     * Update an existing trainee profile.
     *
     * @param updateRequest the request object containing updated trainee details
     * @throws NotFoundException if Trainee not found with given id
     * @return updated TraineeDTO
     */
    @Override
    public TraineeProfileView updateTrainee(TraineeUpdateRequest updateRequest) {
        log.debug("Update Trainee request: {}", updateRequest);

        Trainee trainee = traineeRepository.findByUserUsername(updateRequest.username())
                .orElseThrow(() -> new NotFoundException(
                        "Trainee not found with username: " + updateRequest.username(),
                        ErrorResponse.ErrorPointer.username)
                );

        // update user details trainee belongs to
        User user = trainee.getUser();

        // apply changes to user
        userService.updateUser(user.getId(), new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()));

        // apply changes to training
        if(StringUtils.hasText(updateRequest.address())) {
            trainee.setAddress(updateRequest.address());
        }
        if(Objects.nonNull(updateRequest.dateOfBirth())) {
            trainee.setDateOfBirth(updateRequest.dateOfBirth());
        }
        trainee.setUpdatedAt(LocalDateTime.now());

        traineeRepository.save(trainee);

        List<TraineeProfileTrainerDTO> trainerDTOS = trainerTraineeRelationService
                .findTrainersOfTraineeByTraineeUsername(updateRequest.username());

        return new TraineeProfileView(
                user.getFirstName(),
                user.getLastName(),
                trainee.getAddress(),
                trainee.getDateOfBirth(),
                trainee.isActive(),
                trainerDTOS
        );
    }

    /**
     * Delete a trainee by username.
     *
     * @param traineeUsername the username of the trainee to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTrainee(String traineeUsername) {

        Optional<Trainee> optionalTrainee = traineeRepository.findByUserUsername(traineeUsername);

        if (optionalTrainee.isEmpty()) {
            log.debug("Trainee not found with username: {}", traineeUsername);
            return false;
        }

        Trainee trainee = optionalTrainee.get();

        // TODO: I know this is not efficient way, but I implemented it to fill the task requirement
        List<Training> trainings = trainee.getTrainings();
        trainings.forEach(training -> {
            workloadPublisher.publishDelete(training, training.getTrainer());
        });

        // at the end delete the trainee and trainings it belongs
        traineeRepository.deleteByUserUsername(traineeUsername);
        return true;
    }

    /**
     * Check if trainee exists by ID
     *
     * @param traineeId the id of trainee
     * @return true if trainee exists, false does not
     */
    @Override
    public boolean existsById(Long traineeId) {
        return traineeRepository.existsById(traineeId);
    }

    /**
     * Get reference to Trainee entity
     *
     * @param traineeId the id of Trainee
     * @return Trainee reference to Trainee entity
     */
    @Transactional(readOnly = true)
    @Override
    public Trainee getReferenceById(Long traineeId) {
        return traineeRepository.getReferenceById(traineeId);
    }


    /**
     * Get all Trainings of Trainee according to given searchCriteria
     *
     * @param searchCriteria containing criteria data for filtering
     * @return list of TrainingView
     */
    @Transactional(readOnly = true)
    @Override
    public List<TraineeTrainingProfileView> findTrainingsOfTraineeByCriteria(TrainingsOfTraineeSearchCriteria searchCriteria) {

        return traineeRepository.findTrainingsOfTraineeByCriteria(
                searchCriteria.traineeUsername(),
                searchCriteria.fromDate(),
                searchCriteria.toDate(),
                searchCriteria.trainerName(),
                searchCriteria.typeId()
        );
    }

    /**
     * Check if trainee exists by username
     *
     * @param username the login username of the trainee
     * @return {@code true} if a trainee with the given username exists
     */
    @Override
    public boolean existsByUsername(String username) {
        return traineeRepository.existsByUserUsername(username);
    }

    /**
     * Find the Trainee entity by username.
     *
     * @param username the trainee's login username
     * @return the Trainee entity
     * @throws org.example.exception.model.NotFoundException if no trainee with the given username exists
     */
    @Override
    public Trainee findTraineeByUsername(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException(
                        "Trainee not found by username: " + username,
                        ErrorResponse.ErrorPointer.username
                ));
    }

    /**
     * Activate or deactivate the trainee identified in the request.
     *
     * @param statusRequest payload with the trainee's username and desired active state
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
     * Activate Trainee with given ID
     *
     * @throws RuntimeException if trainee not found or already active
     * @throws NotFoundException if Trainee not found with given id
     * @param traineeUsername the username of the trainee
     */
    private void activate(String traineeUsername) {
        Trainee trainee = findTraineeByUsername(traineeUsername);

        if(trainee.isActive()) {
            throw new RuntimeException("Trainee already active");
        }
        trainee.setActive(true);
        traineeRepository.save(trainee);
    }

    /**
     * Deactivate Trainee with given ID
     *
     * @throws RuntimeException if trainee not found or already inactive
     * @throws NotFoundException if Trainee not found with given id
     * @param traineeUsername the username of the trainee
     */
    private void deactivate(String traineeUsername) {
        Trainee trainee = findTraineeByUsername(traineeUsername);

        if(!trainee.isActive()) {
            throw new RuntimeException("Trainee already inactive");
        }
        trainee.setActive(false);
        traineeRepository.save(trainee);
    }
}
