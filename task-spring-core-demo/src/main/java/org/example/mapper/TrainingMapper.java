package org.example.mapper;

import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {


    public Training toEntity(TrainingCreateRequest createRequest) {
        Training training = new Training();
        training.setName(createRequest.name());
        training.setDate(createRequest.date());
        training.setDuration(createRequest.duration());
        return training;
    }

    public TrainingDTO toDTO(Training training, Long traineeId, Long trainerId, Integer typeId) {
        return new TrainingDTO(
                training.getId(),
                traineeId,
                trainerId,
                training.getName(),
                typeId,
                training.getDate(),
                training.getDuration(),
                training.getCreatedAt(),
                training.getUpdatedAt()
        );
    }
}
