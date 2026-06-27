package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TraineeUpdateTrainersRequest(
        @NotBlank(message = "Trainee username can't be blank")
        String traineeUsername,
        @NotEmpty(message = "Trainers list can't be empty")
        List<TrainerUsernameDTO> trainers
) {
        public record TrainerUsernameDTO(
                @NotBlank(message = "Trainer username can't be blank")
                String username
        ) {}
}