package org.example.dto.response;

import java.util.List;

public record TrainerProfileView(
        String firstName,
        String lastName,
        Integer specializationId,
        boolean isActive,
        List<TrainerProfileTraineeDTO> trainees
) {
}
