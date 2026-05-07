package org.example.mapper;

import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public TrainingDTO toDTO(Training training) {
        return new TrainingDTO(
                training.getId(),
                training.getTraineeId(),
                training.getTrainerId(),
                training.getName(),
                training.getType(),
                training.getDate(),
                training.getDuration(),
                training.getCreatedAt()
        );
    }

    public Training toEntity(TrainingCreateRequest createRequest) {
        Training training = new Training();
        training.setTraineeId(createRequest.traineeId());
        training.setTrainerId(createRequest.trainerId());
        training.setName(createRequest.name());
        training.setType(createRequest.type());
        training.setDate(createRequest.date());
        training.setDuration(createRequest.duration());
        return training;
    }
}
