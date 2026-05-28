package org.example.service.api;

import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;

public interface TrainingService {

    /**
     * Create a new training session between an already-linked trainer and trainee.
     *
     * @param createRequest payload with trainee/trainer usernames, name, date, and duration
     * @return TrainingDTO representing the persisted training
     * @throws org.example.exception.model.NotFoundException if the trainer–trainee relationship does not exist
     */
    TrainingDTO createTraining(TrainingCreateRequest createRequest);

    /**
     * Soft-delete a training by ID (sets {@code active = false}).
     *
     * @param trainingId the ID of the training to delete
     * @return {@code true} if a row was affected, {@code false} if no matching training found
     */
    boolean deleteTraining(Long trainingId);
}
