package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TrainerTraineeRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TrainerTraineeRelationServiceImpl implements TrainerTraineeRelationService {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;

    /**
     * Replace the full trainer list of a trainee with the trainers provided in the request.
     *
     * @param request payload with trainee username and ordered list of trainer usernames
     * @return updated list of assigned trainer DTOs
     * @throws org.example.exception.model.NotFoundException if the trainee or any trainer is not found
     */
    @Override
    public List<TraineeProfileTrainerDTO> updateTraineeTrainers(TraineeUpdateTrainersRequest request) {

        log.debug("Update trainee {} trainers with: {}", request.traineeUsername(), request.trainers());

        // Find trainee
        Trainee trainee = traineeRepository.findByUserUsername(request.traineeUsername())
                .orElseThrow(() -> new NotFoundException(
                        "Trainee not found with username: " + request.traineeUsername(),
                        ErrorResponse.ErrorPointer.username)
                );

        // Extract trainer usernames from request
        List<String> trainerUsernames = request.trainers().stream()
                .map(TraineeUpdateTrainersRequest.TrainerUsernameDTO::username)
                .distinct()
                .toList();

        // Find all trainers by usernames
        List<Trainer> newTrainers = trainerRepository.findAllByUsernames(trainerUsernames);

        if (newTrainers.size() != trainerUsernames.size()) {
            throw new NotFoundException(
                    "Some trainers not found. Expected: " + trainerUsernames.size() +", Found: " + newTrainers.size(),
                    ErrorResponse.ErrorPointer.username
            );
        }

        // Get current trainers and remove trainee from their lists
        List<Trainer> currentTrainers = trainerRepository.findAllByTraineesId(trainee.getId());
        for (Trainer currentTrainer : currentTrainers) {
            currentTrainer.getTrainees().remove(trainee);
        }

        // Clear trainee's trainers and add new ones
        trainee.getTrainers().clear();
        for (Trainer newTrainer : newTrainers) {
            trainee.getTrainers().add(newTrainer);
            if (!newTrainer.getTrainees().contains(trainee)) {
                newTrainer.getTrainees().add(trainee);
            }
        }

        // Persist changes
        traineeRepository.save(trainee);
        trainerRepository.saveAll(newTrainers);
        trainerRepository.saveAll(currentTrainers);

        log.debug("Trainee {} trainers updated successfully", request.traineeUsername());

        // Return updated trainers list
        return newTrainers.stream()
                .map(trainer -> {
                    User user = trainer.getUser();
                    return new TraineeProfileTrainerDTO(
                            user.getUsername(),
                            user.getFirstName(),
                            user.getLastName(),
                            trainer.getSpecialization().getId()
                    );
                })
                .toList();
    }

    /**
     * Check if Trainer Trainee relationship exists according to trainerId and traineeId
     *
     * @param trainerUsername the username of Trainer
     * @param traineeUsername the username of Trainee
     * @return boolean true of exists, false it does not exist
     */
    @Override
    public boolean existsTrainerTraineeRelation(String trainerUsername, String traineeUsername) {
        return trainerRepository.existsTrainerTraineeRelation(trainerUsername, traineeUsername);
    }

    /**
     * Return all trainers currently assigned to the specified trainee.
     *
     * @param traineeUsername the trainee's login username
     * @return list of trainer DTOs assigned to the trainee
     */
    @Override
    public List<TraineeProfileTrainerDTO> findTrainersOfTraineeByTraineeUsername(String traineeUsername) {
        return trainerRepository.findTrainersOfTraineeByTraineeUsername(traineeUsername);
    }

    /**
     * Return all trainees currently assigned to the specified trainer.
     *
     * @param trainerUsername the trainer's login username
     * @return list of trainee DTOs assigned to the trainer
     */
    @Override
    public List<TrainerProfileTraineeDTO> findTraineesOfTrainerByTrainerUsername(String trainerUsername) {
        return trainerRepository.findTraineesOfTrainerByTrainerUsername(trainerUsername);
    }
}
