package org.example.dto.request;

import org.example.entity.TrainingType;

public record TrainerCreateRequest(
        String lastName,
        String firstName,
        TrainingType specialization
) {
}
