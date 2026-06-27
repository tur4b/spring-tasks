package org.example.dto.response;

import org.example.entity.TrainingTypeName;

public record TrainingTypeDTO(
        Integer id,
        TrainingTypeName name) {
}
