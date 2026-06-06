package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.*;
import org.example.dto.response.BaseResponse;
import org.example.dto.response.TrainerTrainingProfileView;
import org.example.dto.response.UserCredentialsDTO;
import org.example.service.api.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Trainer operations.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/trainers")
@Tag(name = "Trainers")
public class TrainerController {

    private final TrainerService trainerService;

    /**
     * Register a new trainer.
     *
     * @param registrationRequest validated trainer registration details
     * @return ResponseEntity with 201 status and UserCredentialsDTO
     */
    @PostMapping
    @SecurityRequirements({})
    @Operation(summary = "Register trainer")
    public ResponseEntity<BaseResponse<UserCredentialsDTO>> registerTrainer(
            @Valid @RequestBody TrainerCreateRequest registrationRequest) {

        UserCredentialsDTO credentials = trainerService.createTrainer(registrationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new BaseResponse<>(
                        credentials,
                        "Trainer registered successfully. Please save your credentials."
                ));
    }

    /**
     * Get trainer profile details by username.
     *
     * @param trainerUsername unique trainer username
     * @return trainer profile view response
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    public ResponseEntity<BaseResponse<?>> getTrainerProfile(
            @NotBlank(message = "Username can't be blank") @PathVariable("username") String trainerUsername) {
        var trainers = trainerService.findTrainerViewByUsername(trainerUsername);

        return ResponseEntity
                .ok()
                .body(new BaseResponse<>(
                        trainers,
                        "Trainer profile view"
                ));
    }

    /**
     * Update trainer profile information.
     *
     * @param updateRequest validated trainer update payload
     * @return updated trainer profile view response
     */
    @PutMapping
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<BaseResponse<?>> updateTrainee(@Valid @RequestBody TrainerUpdateRequest updateRequest) {

        var trainerProfileView = trainerService.updateTrainer(updateRequest);

        return ResponseEntity.ok(
                new BaseResponse<>(trainerProfileView, "Trainer profile updated successfully")
        );
    }

    /**
     * Get trainers not currently assigned to a trainee.
     *
     * @param traineeUsername unique trainee username
     * @return list of trainers not assigned to the trainee
     */
    @GetMapping("/not-assigned-on-trainee/{traineeUsername}")
    @Operation(summary = "Get trainers not assigned to trainee")
    public ResponseEntity<BaseResponse<?>> notAssignedOnTraineeTrainers(
            @NotBlank(message = "Username can't be blank") @PathVariable("traineeUsername") String traineeUsername) {

        var trainers = trainerService.findTrainersNotAssignedToTrainee(traineeUsername);

        return ResponseEntity.ok(
                new BaseResponse<>(
                        trainers,
                        "Trainers list not assigned on trainee"
                )
        );
    }

    /**
     * Get trainer trainings filtered by search criteria.
     *
     * @param searchCriteria validated training search filters
     * @return filtered trainer trainings list
     */
    @GetMapping("/trainings")
    @Operation(summary = "Get trainer trainings by criteria")
    public ResponseEntity<BaseResponse<?>> getTrainerTrainingsList(@Valid TrainingsOfTrainerSearchCriteria searchCriteria) {

        List<TrainerTrainingProfileView> trainings = trainerService.findTrainingsOfTrainerByCriteria(searchCriteria);

        return ResponseEntity.ok(
                new BaseResponse<>(
                        trainings,
                        "Trainee trainings list"
                )
        );
    }

    /**
     * Update trainer activation status.
     *
     * @param statusRequest validated status update payload
     * @return OK when status update succeeds
     */
    @PatchMapping("/status")
    @Operation(summary = "Update trainer active status")
    public ResponseEntity<BaseResponse<?>> statusUpdate(@Valid @RequestBody UpdateStatusRequest statusRequest) {
        trainerService.updateStatus(statusRequest);
        return ResponseEntity.ok().build();
    }

}
