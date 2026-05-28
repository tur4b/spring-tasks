package org.example.service.api;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.Trainer;

import java.util.List;

public interface TrainerService {

    /**
     * Register a new trainer and auto-generate login credentials.
     *
     * @param trainerCreateRequest creation payload with first/last name and specialization ID
     * @return UserCredentialsDTO containing the generated username and raw password
     * @throws org.example.exception.model.NotFoundException if the specialization ID does not exist
     */
    UserCredentialsDTO createTrainer(TrainerCreateRequest trainerCreateRequest);

    /**
     * Update an existing trainer's profile fields.
     *
     * @param trainerUpdateRequest update payload identified by username
     * @return updated TrainerProfileView
     * @throws org.example.exception.model.NotFoundException if the trainer or specialization is not found
     */
    TrainerProfileView updateTrainer(TrainerUpdateRequest trainerUpdateRequest);

    /**
     * Check whether a trainer exists by internal ID.
     *
     * @param trainerId the trainer entity ID
     * @return {@code true} if exists
     */
    boolean existsById(Long trainerId);

    /**
     * Find trainer trainings filtered by the given search criteria.
     *
     * @param searchCriteria filter parameters (username, date range, trainee name, type)
     * @return list of matching training profile views
     */
    List<TrainerTrainingProfileView> findTrainingsOfTrainerByCriteria(TrainingsOfTrainerSearchCriteria searchCriteria);

    /**
     * Return the list of active trainers who are not yet assigned to the specified trainee.
     *
     * @param traineeUsername the trainee's login username
     * @return list of unassigned trainer DTOs; empty list if trainee does not exist
     */
    List<TraineeProfileTrainerDTO> findTrainersNotAssignedToTrainee(String traineeUsername);

    /**
     * Retrieve a full trainer profile view by username, including assigned trainees.
     *
     * @param trainerUsername the trainer's login username
     * @return TrainerProfileView aggregated profile
     * @throws org.example.exception.model.NotFoundException if no trainer matches
     */
    TrainerProfileView findTrainerViewByUsername(String trainerUsername);

    /**
     * Delete the trainer identified by username.
     *
     * @param traineeUsername the trainer's login username
     * @return {@code true} if deleted, {@code false} if no matching trainer found
     */
    boolean deleteTrainer(String traineeUsername);

    /**
     * Find the Trainer entity by username.
     *
     * @param username the trainer's login username
     * @return the Trainer entity
     * @throws org.example.exception.model.NotFoundException if not found
     */
    Trainer findTrainerByUsername(String username);

    /**
     * Activate or deactivate a trainer based on the request payload.
     *
     * @param statusRequest payload with username and desired active flag
     */
    void updateStatus(UpdateStatusRequest statusRequest);
}
