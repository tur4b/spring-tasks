package org.example.dto.response;

public record TraineeProfileTrainerDTO(
        String username,
        String firstName,
        String lastName,
        Integer specializationId
) {
}
