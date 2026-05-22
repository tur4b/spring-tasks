package org.example.mapper;

import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeMapper {

    public TrainingTypeDTO toDTO(TrainingType trainingType) {
        return new TrainingTypeDTO(trainingType.getId(), trainingType.getName());
    }
}
