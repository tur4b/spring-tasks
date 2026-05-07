package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TrainerDAO;
import org.example.dto.request.*;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainer;
import org.example.mapper.TrainerMapper;
import org.example.service.api.TrainerService;
import org.example.service.api.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for trainer operations
 */
@Slf4j
@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerMapper trainerMapper;
    private final TrainerDAO trainerDAO;
    private final UserService userService;

    /**
     * Constructor for <code>TrainerServiceImpl</code>
     * constructor injection applied for required dependencies
     *
     * @param trainerMapper TrainerMapper instance
     * @param trainerDAO    TrainerDAO instance
     * @param userService   UserService instance
     */
    public TrainerServiceImpl(TrainerMapper trainerMapper,
                              TrainerDAO trainerDAO,
                              UserService userService) {
        this.trainerMapper = trainerMapper;
        this.trainerDAO = trainerDAO;
        this.userService = userService;
    }

    /**
     * Get list of TrainerDTO
     *
     * @return list of trainers that converted to dtos
     */
    @Override
    public List<TrainerDTO> findAll() {
        log.debug("Find All Trainer");
        return trainerDAO.findAll()
                .stream()
                .map(trainerMapper::toDTO)
                .toList();
    }

    /**
     * Get TrainerDTO by trainer ID
     *
     * @param trainerId the ID of the trainer
     * @return TrainerDTO corresponding to the given ID
     */
    @Override
    public TrainerDTO findTrainerById(Long trainerId) {
        log.debug("Find Trainer by ID: {}", trainerId);
        return trainerDAO.findById(trainerId)
                .map(trainerMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
    }

    /**
     * Create a new trainer with auto-generated credentials.
     * Handles duplicate username scenarios by appending a serial number.
     *
     * @param trainerCreateRequest the request object containing trainer details
     * @return created TrainerDTO with generated username and password
     */
    @Override
    public TrainerDTO createTrainer(TrainerCreateRequest trainerCreateRequest) {
        if(trainerCreateRequest == null) {
            log.error("TrainerCreateRequest cannot be null");
            throw new IllegalArgumentException("TrainerCreateRequest cannot be null");
        }

        log.debug("Create Trainer request: {}", trainerCreateRequest);

        // creating a new user
        UserDTO userDTO = userService.createUser(new UserCreateRequest(trainerCreateRequest.firstName(), trainerCreateRequest.lastName()));

        // create a new trainer and set required fields
        Trainer trainer = trainerMapper.toEntity(trainerCreateRequest);
        trainer.setUserId(userDTO.id());
        trainer.setActive(true);

        Trainer createdTrainer = trainerDAO.create(trainer);
        log.debug("Trainer created with username: {}", userDTO.username());

        return trainerMapper.toDTO(createdTrainer);
    }

    /**
     * Update an existing trainer profile.
     *
     * @param trainerId the ID of the trainer to update
     * @param updateRequest the request object containing updated trainer details
     * @return updated TrainerDTO
     */
    @Override
    public TrainerDTO updateTrainer(Long trainerId, TrainerUpdateRequest updateRequest) {
        if(trainerId == null || updateRequest == null) {
            log.error("Trainer ID and TrainerUpdateRequest cannot be null");
            throw new IllegalArgumentException("TrainerId and TrainerUpdateRequest cannot be null");
        }

        log.debug("Update Trainer id: {} and request: {}", trainerId, updateRequest);

        Trainer trainer = trainerDAO.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));

        // update user details trainer belongs to
        UserDTO userDTO = userService.findUserById(trainer.getUserId());
        // apply changes to user
        userService.updateUser(userDTO.id(), new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()));

        // apply changes to training
        trainer.setSpecialization(updateRequest.specialization());
        trainer.setUpdatedAt(LocalDateTime.now());

        trainerDAO.update(trainer);

        return trainerMapper.toDTO(trainer);
    }

    /**
     * Soft Delete a trainer by ID.
     *
     * @param trainerId the ID of the trainer to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteTrainer(Long trainerId) {
        return trainerDAO.deleteById(trainerId);
    }

    /**
     * Check if trainer exists by ID
     *
     * @param trainerId the id of trainer
     * @return true if trainer exists, false does not
     */
    @Override
    public boolean existsById(Long trainerId) {
        return trainerDAO.existsById(trainerId);
    }
}
