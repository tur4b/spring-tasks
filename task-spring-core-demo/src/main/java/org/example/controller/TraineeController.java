package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerTraineeRelationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Trainee operations.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerTraineeRelationService trainerTraineeRelationService;

    /**
     * Register a new trainee.
     *
     * @param registrationRequest validated trainee registration details
     * @return ResponseEntity with 201 status and UserCredentialsDTO
     */
    @PostMapping
    public ResponseEntity<BaseResponse<UserCredentialsDTO>> registerTrainee(
            @Valid @RequestBody TraineeCreateRequest registrationRequest) {

        UserCredentialsDTO credentials = traineeService.createTrainee(registrationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new BaseResponse<>(
                        credentials,
                        "Trainee registered successfully."
                ));
    }

    /**
     * Get trainee profile details by username.
     *
     * @param traineeUsername unique trainee username
     * @return trainee profile view response
     */
    @GetMapping("/{username}")
    public ResponseEntity<BaseResponse<?>> getTraineeProfile(
            @NotBlank(message = "Username can't be blank") @PathVariable("username") String traineeUsername) {

        var trainees = traineeService.findTraineeViewByUsername(traineeUsername);

        return ResponseEntity
                .ok()
                .body(new BaseResponse<>(
                        trainees,
                        "Trainee profile view"
                ));
    }

    /**
     * Update trainee profile information.
     *
     * @param updateRequest validated trainee update payload
     * @return updated trainee profile view response
     */
    @PutMapping
    public ResponseEntity<BaseResponse<?>> updateTrainee(@Valid @RequestBody TraineeUpdateRequest updateRequest) {

        var traineeProfileView = traineeService.updateTrainee(updateRequest);

        return ResponseEntity.ok(
                new BaseResponse<>(traineeProfileView, "Trainee profile updated successfully")
        );
    }

    /**
     * Delete trainee profile by username.
     *
     * @param traineeUsername unique trainee username
     * @return completion response
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteTrainee(
            @NotBlank(message = "Username can't be blank") @PathVariable("username") String traineeUsername) {

        traineeService.deleteTrainee(traineeUsername);
        return ResponseEntity.ok(
                new BaseResponse<>(null, "Delete operation completed")
        );
    }

    /**
     * Replace trainee's assigned trainers list.
     *
     * @param updateRequest validated trainee-trainer assignment payload
     * @return updated list of assigned trainers
     */
    @PutMapping("/{username}/trainers")
    public ResponseEntity<BaseResponse<?>> updateTraineeTrainersList(
            @Valid @RequestBody TraineeUpdateTrainersRequest updateRequest) {

        List<TraineeProfileTrainerDTO> trainers = trainerTraineeRelationService.updateTraineeTrainers(updateRequest);

        return ResponseEntity.ok(
                new BaseResponse<>(
                        trainers,
                        "Trainee trainers updated successfully"
                )
        );
    }

    /**
     * Get trainee trainings filtered by search criteria.
     *
     * @param searchCriteria validated training search filters
     * @return filtered trainee trainings list
     */
    @GetMapping("/trainings")
    public ResponseEntity<BaseResponse<?>> getTraineeTrainingsList(@Valid TrainingsOfTraineeSearchCriteria searchCriteria) {

        List<TraineeTrainingProfileView> trainings = traineeService.findTrainingsOfTraineeByCriteria(searchCriteria);

        return ResponseEntity.ok(
                new BaseResponse<>(
                        trainings,
                        "Trainee trainings list"
                )
        );
    }

    /**
     * Update trainee activation status.
     *
     * @param statusRequest validated status update payload
     * @return OK when status update succeeds
     */
    @PatchMapping("/status")
    public ResponseEntity<BaseResponse<?>> statusUpdate(@Valid @RequestBody UpdateStatusRequest statusRequest) {

        traineeService.updateStatus(statusRequest);
        return ResponseEntity.ok().build();
    }
}
