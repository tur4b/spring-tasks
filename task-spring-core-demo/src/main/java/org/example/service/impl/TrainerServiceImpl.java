package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aspect.Secured;
import org.example.dao.TrainerRepository;
import org.example.dao.projection.TrainerView;
import org.example.dao.projection.TrainingView;
import org.example.dto.request.*;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.mapper.TrainerMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
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

    private final TrainerMapper trainerMapper;
    private final TrainerRepository trainerRepository;
    private final UserService userService;
    private final TrainingTypeService trainingTypeService;
    private final TraineeService traineeService;

    /**
     * Get list of TrainerView
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of projected trainers as TrainerView
     */
    @Secured
    @Override
    public List<TrainerView> findAllTrainersView(AuthRequest authRequest) {
        log.debug("Find All Trainer");
        return trainerRepository.findAllTrainersView();
    }

    /**
     * Get TrainerView by trainer ID
     *
     * @param trainerId the ID of the trainer
     * @param authRequest the instance of AuthRequest containing credentials
     * @return TrainerView corresponding to the given ID
     */
    @Secured
    @Override
    public TrainerView findTrainerViewById(Long trainerId, AuthRequest authRequest) {
        log.debug("Find Trainer by ID: {}", trainerId);
        return trainerRepository.findTrainerViewById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with ID: " + trainerId));
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
        log.debug("Create Trainer request: {}", trainerCreateRequest);

        // creating a new user
        UserDTO userDTO = userService.createUser(
                new UserCreateRequest(
                    trainerCreateRequest.firstName(),
                    trainerCreateRequest.lastName()
                )
        );

        Trainer trainer = new Trainer();

        // set newly create user to trainer
        User createdUserEntityRef = userService.getReferenceById(userDTO.id());
        trainer.setUser(createdUserEntityRef);

        // set specialization
        if (!trainingTypeService.existsById(trainerCreateRequest.specializationId())) {
            throw new EntityNotFoundException("TrainingType not found with ID: " + trainerCreateRequest.specializationId());
        }

        TrainingType specialization = trainingTypeService.getReferenceById(trainerCreateRequest.specializationId());
        trainer.setSpecialization(specialization);

        // save changes
        Trainer createdTrainer = trainerRepository.save(trainer);
        log.debug("Trainer created with username: {}", userDTO.username());

        return trainerMapper.toDTO(trainer, userDTO.id(), trainerCreateRequest.specializationId());
    }

    /**
     * Update an existing trainer profile.
     *
     * @param trainerId the ID of the trainer to update
     * @param updateRequest the request object containing updated trainer details
     * @param authRequest the instance of AuthRequest containing credentials
     * @return updated TrainerDTO
     */
    @Secured
    @Override
    public TrainerDTO updateTrainer(Long trainerId, TrainerUpdateRequest updateRequest, AuthRequest authRequest) {
        log.debug("Update Trainer id: {} and request: {}", trainerId, updateRequest);

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));

        // update user details trainer belongs to
        User user = trainer.getUser();
        userService.updateUser(user.getId(), new UserUpdateRequest(updateRequest.firstName(), updateRequest.lastName()), authRequest);

        // set specialization
        if (!trainingTypeService.existsById(updateRequest.specializationId())) {
            throw new EntityNotFoundException("TrainingType not found with ID: " + updateRequest.specializationId());
        }

        TrainingType specialization = trainingTypeService.getReferenceById(updateRequest.specializationId());
        trainer.setSpecialization(specialization);

        // save changes
        trainerRepository.save(trainer);

        return trainerMapper.toDTO(trainer, user.getId(), updateRequest.specializationId());
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
     * Activate Trainer with given ID
     *
     * @param trainerId the id of the trainer
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if trainer not found
     * @throws RuntimeException if trainer is already active
     */
    @Secured
    @Override
    public void activate(Long trainerId, AuthRequest authRequest) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with ID: " + trainerId));

        if(trainer.isActive()) {
            throw new RuntimeException("Trainer already active with ID: " + trainerId);
        }
        trainer.setActive(true);
        trainerRepository.save(trainer);
    }

    /**
     * Deactivate Trainer with given ID
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if trainer not found
     * @throws RuntimeException if trainer is already inactive
     */
    @Secured
    @Override
    public void deactivate(Long trainerId, AuthRequest authRequest) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with ID: " + trainerId));

        if(!trainer.isActive()) {
            throw new RuntimeException("Trainer already inactive with ID: " + trainerId);
        }
        trainer.setActive(false);
        trainerRepository.save(trainer);
    }

    /**
     * Check if Trainer Trainee relationship exists according to trainerId and traineeId
     *
     * @param trainerId the id of Trainer
     * @param traineeId the id of Trainee
     * @return boolean true of exists, false it does not exist
     */
    @Override
    public boolean existsTrainerTraineeRelation(Long trainerId, Long traineeId) {
        return trainerRepository.existsTrainerTraineeRelation(trainerId, traineeId);
    }

    /**
     * Get reference to Trainer entity
     *
     * @param trainerId the ig of Trainer
     * @return Trainer entity
     */
    @Override
    public Trainer getReferenceById(Long trainerId) {
        return trainerRepository.getReferenceById(trainerId);
    }

    /**
     * ChangePassword according given credentials
     *
     * @param changePasswordRequest the instance of ChangePasswordRequest
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Override
    @Secured
    public void changePassword(ChangePasswordRequest changePasswordRequest,
                               AuthRequest authRequest) {
        if(!trainerRepository.existsByUserUsername(changePasswordRequest.username())) {
            throw new EntityNotFoundException("Trainer not found with username: " + changePasswordRequest.username());
        }
        userService.changePassword(changePasswordRequest, authRequest);
    }

    /**
     * Get all Trainings of Trainer according to given searchCriteria
     *
     * @param searchCriteria containing criteria data for filtering
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of TrainingView
     */
    @Transactional(readOnly = true)
    @Secured
    @Override
    public List<TrainingView> findTrainingsOfTrainerByCriteria(TrainingsOfTrainerSearchCriteria searchCriteria,
                                                               AuthRequest authRequest) {
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
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of TraineeView
     */
    @Transactional(readOnly = true)
    @Secured
    @Override
    public List<TrainerView> findTrainersNotAssignedToTrainee(String traineeUsername, AuthRequest authRequest) {
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }

    /**
     * Reassign Trainee to Trainers
     *
     * @param traineeId id of Trainee
     * @param trainerIds id list of Trainer
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Secured
    @Override
    public void reassignTraineeToTrainers(Long traineeId, List<Long> trainerIds, AuthRequest authRequest) {
        // Optional: remove duplicates to keep behavior deterministic
        List<Long> uniqueTrainerIds = trainerIds.stream().distinct().toList();

        Trainee trainee = traineeService.getReferenceById(traineeId);

        List<Trainer> targetTrainers = trainerRepository.findAllById(uniqueTrainerIds);
        if (targetTrainers.size() != uniqueTrainerIds.size()) {
            throw new EntityNotFoundException("All trainers not found");
        }

        List<Trainer> currentTrainers = trainerRepository.findAllByTraineesId(traineeId);
        for (Trainer current : currentTrainers) {
            current.getTrainees().remove(trainee);
        }

        for (Trainer target : targetTrainers) {
            if (!target.getTrainees().contains(trainee)) {
                target.getTrainees().add(trainee);
            }
        }

        trainerRepository.saveAll(currentTrainers);
        trainerRepository.saveAll(targetTrainers);
    }

}
