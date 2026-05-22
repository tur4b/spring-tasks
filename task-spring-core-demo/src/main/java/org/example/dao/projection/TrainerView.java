package org.example.dao.projection;

import org.example.entity.TrainingTypeName;

import java.time.LocalDateTime;

public interface TrainerView {
    Long getId();
    Long getUserId();
    String getFirstName();
    String getLastName();
    Integer getSpecializationId();
    TrainingTypeName getSpecializationName();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
