package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeDAO;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.mapper.TraineeMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for trainee operations
 */
@Slf4j
@Service
public class TraineeServiceImpl implements TraineeService {

    private final TraineeMapper traineeMapper;
    private final TraineeDAO traineeDAO;
    private final UserService userService;

    /**
     * Constructor for <code>TraineeServiceImpl</code>
     * constructor injection applied for required dependencies
     *
     * @param traineeMapper TraineeMapper instance
     * @param traineeDAO    TraineeDAO instance
     * @param userService   UserService instance
     */
    public TraineeServiceImpl(TraineeMapper traineeMapper,
                              TraineeDAO traineeDAO,
                              UserService userService) {
        this.traineeMapper = traineeMapper;
        this.traineeDAO = traineeDAO;
        this.userService = userService;
    }

    /**
     * Get list of TraineeDTO
     *
     * @return list of trainees that converted to dtos
     */
    @Override
    public List<TraineeDTO> findAll() {
        log.debug("Find All Trainee");
        return traineeDAO.findAll()
                .stream()
                .map(traineeMapper::toDTO)
                .toList();
    }

    /**
     * Get TraineeDTO by trainee ID
     *
     * @param traineeId the ID of the trainee
     * @return TraineeDTO corresponding to the given ID
     */
    @Override
    public TraineeDTO findTraineeById(Long traineeId) {
        log.debug("Find Trainee by ID: {}", traineeId);
        return traineeDAO.findById(traineeId)
                .map(traineeMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + traineeId));
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
        if(createRequest == null) {
            log.error("TraineeCreateRequest cannot be null");
            throw new IllegalArgumentException("TraineeCreateRequest cannot be null");
        }

        log.debug("Create Trainee request: {}", createRequest);

        // creating a new user
        UserDTO userDTO = userService.createUser(new UserCreateRequest(createRequest.firstName(), createRequest.lastName()));

        // create a new trainee and set required fields
        Trainee trainee = traineeMapper.toEntity(createRequest);
        trainee.setUserId(userDTO.id());

        Trainee createdTrainee = traineeDAO.create(trainee);
        log.debug("Trainee created with username: {}", userDTO.username());

        return traineeMapper.toDTO(createdTrainee);
    }

    /**
     * Update an existing trainee profile.
     *
     * @param traineeId the ID of the trainee to update
     * @param updateRequest the request object containing updated trainee details
     * @return updated TraineeDTO
     */
    @Override
    public TraineeDTO updateTrainee(Long traineeId, TraineeUpdateRequest updateRequest) {
        if(traineeId == null || updateRequest == null) {
            log.error("Trainee ID and TraineeUpdateRequest cannot be null");
            throw new IllegalArgumentException("TraineeId and TraineeUpdateRequest cannot be null");
        }

        log.debug("Update Trainee id: {} and request: {}", traineeId, updateRequest);

        Trainee trainee = traineeDAO.findById(traineeId)
                .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + traineeId));

        // update user details trainee belongs to
        UserDTO userDTO = userService.findUserById(trainee.getUserId());
        // apply changes to user
        userService.updateUser(userDTO.id(), new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()));

        // apply changes to training
        trainee.setAddress(updateRequest.address());
        trainee.setDateOfBirth(updateRequest.dateOfBirth());
        trainee.setUpdatedAt(LocalDateTime.now());

        traineeDAO.update(trainee);

        return traineeMapper.toDTO(trainee);
    }

    /**
     * Delete a trainee by ID.
     *
     * @param traineeId the ID of the trainee to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTrainee(Long traineeId) {
        return traineeDAO.deleteById(traineeId);
    }

    /**
     * Check if trainee exists by ID
     *
     * @param traineeId the id of trainee
     * @return true if trainee exists, false does not
     */
    @Override
    public boolean existsById(Long traineeId) {
        return traineeDAO.existsById(traineeId);
    }
}
