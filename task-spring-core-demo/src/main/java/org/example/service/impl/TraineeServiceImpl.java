package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aspect.Secured;
import org.example.dao.TraineeRepository;
import org.example.dao.projection.TraineeView;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.*;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.mapper.TraineeMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for trainee operations
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
@Service
public class TraineeServiceImpl implements TraineeService {

    private final TraineeMapper traineeMapper;
    private final TraineeRepository traineeRepository;
    private final UserService userService;

    /**
     * Get list of TraineeView
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of projected view of trainees
     */
    @Transactional(readOnly = true)
    @Secured
    @Override
    public List<TraineeView> findAllTraineesView(AuthRequest authRequest) {
        log.debug("Find All Trainees");
        return traineeRepository.findAllTraineesView();
    }

    /**
     * Get TraineeView by trainee ID
     *
     * @param traineeId the ID of the trainee
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if Trainee not found with given id
     * @return TraineeView corresponding to the given ID
     */
    @Transactional(readOnly = true)
    @Secured
    @Override
    public TraineeView findTraineeViewById(Long traineeId, AuthRequest authRequest) {
        log.debug("Find Trainee by ID: {}", traineeId);
        return traineeRepository.findTraineeViewById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with given id: " + traineeId));
    }

    /**
     * Create a new trainee with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param createRequest the request object containing trainee details
     * @return created TraineeDTO with generated username and password
     */
    @Override
    public TraineeDTO createTrainee(TraineeCreateRequest createRequest) {
        log.debug("Create Trainee request: {}", createRequest);

        // creating a new user
        UserDTO userDTO = userService.createUser(new UserCreateRequest(createRequest.firstName(), createRequest.lastName()));

        // create a new trainee and set required fields
        Trainee trainee = traineeMapper.toEntity(createRequest);

        User userEntityRef = userService.getReferenceById(userDTO.id());
        trainee.setUser(userEntityRef);

        traineeRepository.save(trainee);
        log.debug("Trainee created with username: {}", userDTO.username());

        return traineeMapper.toDTO(trainee, userDTO.id());
    }

    /**
     * Update an existing trainee profile.
     *
     * @param traineeId the ID of the trainee to update
     * @param updateRequest the request object containing updated trainee details
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if Trainee not found with given id
     * @return updated TraineeDTO
     */
    @Secured
    @Override
    public TraineeDTO updateTrainee(Long traineeId, TraineeUpdateRequest updateRequest, AuthRequest authRequest) {
        log.debug("Update Trainee id: {} and request: {}", traineeId, updateRequest);

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with ID: " + traineeId));

        // update user details trainee belongs to
        User user = trainee.getUser();

        // apply changes to user
        userService.updateUser(user.getId(), new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()), authRequest);

        // apply changes to training
        trainee.setAddress(updateRequest.address());
        trainee.setDateOfBirth(updateRequest.dateOfBirth());
        trainee.setUpdatedAt(LocalDateTime.now());

        traineeRepository.save(trainee);

        return traineeMapper.toDTO(trainee, user.getId());
    }

    /**
     * Delete a trainee by username.
     *
     * @param traineeUsername the username of the trainee to delete
     * @param authRequest the instance of AuthRequest containing credentials
     * @return true if deletion was successful, false otherwise
     */
    @Secured
    @Override
    public boolean deleteTraineeByUsername(String traineeUsername, AuthRequest authRequest) {
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElse(null);

        if(trainee == null) {
            log.debug("Trainee not found with username: {}", traineeUsername);
            return false;
        }
        traineeRepository.deleteById(trainee.getId());
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
     * Activate Trainee with given ID
     *
     * @throws RuntimeException if trainee not found or already active
     * @throws EntityNotFoundException if Trainee not found with given id
     * @param traineeId the id of the trainee
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Secured
    @Override
    public void activate(Long traineeId, AuthRequest authRequest) {
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with ID: " + traineeId));

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
     * @throws EntityNotFoundException if Trainee not found with given id
     * @param traineeId the id of the trainee
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Secured
    @Override
    public void deactivate(Long traineeId, AuthRequest authRequest) {
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with ID: " + traineeId));

        if(!trainee.isActive()) {
            throw new RuntimeException("Trainee already deactive");
        }
        trainee.setActive(false);
        traineeRepository.save(trainee);
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
     * ChangePassword according given credentials
     *
     * @param changePasswordRequest the instance of ChangePasswordRequest
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Secured
    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, AuthRequest authRequest) {
        if(!traineeRepository.existsByUserUsername(changePasswordRequest.username())) {
            throw new EntityNotFoundException("Trainee not found with username: " + changePasswordRequest.username());
        }
        userService.changePassword(changePasswordRequest, authRequest);
    }

    /**
     * Get all Trainings of Trainee according to given searchCriteria
     *
     * @param searchCriteria containing criteria data for filtering
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of TrainingView
     */
    @Secured
    @Transactional(readOnly = true)
    @Override
    public List<TrainingView> findTrainingsOfTraineeByCriteria(TrainingsOfTraineeSearchCriteria searchCriteria,
                                                               AuthRequest authRequest) {
        return traineeRepository.findTrainingsOfTraineeByCriteria(
                searchCriteria.traineeUsername(),
                searchCriteria.fromDate(),
                searchCriteria.toDate(),
                searchCriteria.trainerName(),
                searchCriteria.typeId()
        );
    }



}
