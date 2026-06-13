package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.config.security.JwtService;
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
@Tag(name = "Trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerTraineeRelationService trainerTraineeRelationService;
    private final JwtService jwtService;

    /**
     * Register a new trainee.
     *
     * @param registrationRequest validated trainee registration details
     * @return ResponseEntity with 201 status and UserCredentialsDTO
     */
    @PostMapping
    @SecurityRequirements({})
    @Operation(summary = "Register trainee")
    public ResponseEntity<BaseResponse<UserCredentialsDTO>> registerTrainee(
            @Valid @RequestBody TraineeCreateRequest registrationRequest) {

        UserCredentialsDTO credentials = traineeService.createTrainee(registrationRequest);
        String accessToken = jwtService.generateToken(credentials.username());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("X-Auth-Token", accessToken)
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
    @Operation(summary = "Get trainee profile by username")
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
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<BaseResponse<?>> updateTrainee(@Valid @RequestBody TraineeUpdateRequest updateRequest) {

        var traineeProfileView = traineeService.updateTrainee(updateRequest);

        return ResponseEntity.ok(
                new BaseResponse<>(traineeProfileView, "Trainee profile updated successfully")
        );
    }

    /**
     * Replace trainee's assigned trainers list.
     *
     * @param updateRequest validated trainee-trainer assignment payload
     * @return updated list of assigned trainers
     */
    @PutMapping("/{username}/trainers")
    @Operation(summary = "Replace trainee trainers list")
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
    @Operation(summary = "Get trainee trainings by criteria")
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
    @Operation(summary = "Update trainee active status")
    public ResponseEntity<BaseResponse<?>> statusUpdate(@Valid @RequestBody UpdateStatusRequest statusRequest) {

        traineeService.updateStatus(statusRequest);
        return ResponseEntity.ok().build();
    }
}
