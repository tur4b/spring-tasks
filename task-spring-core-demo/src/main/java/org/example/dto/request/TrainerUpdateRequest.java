package org.example.dto.request;

import org.example.entity.TrainingType;

public record TrainerUpdateRequest(
        String lastName,
        String firstName,
        TrainingType specialization
) {
}
