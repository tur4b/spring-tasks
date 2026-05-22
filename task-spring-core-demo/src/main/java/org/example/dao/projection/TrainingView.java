package org.example.dao.projection;

import org.example.entity.TrainingTypeName;

import java.time.LocalDate;

public interface TrainingView {

    Long getId();
    String getName();
    Long getTrainerId();
    Long getTraineeId();
    Integer getTypeId();
    TrainingTypeName getTypeName();
    LocalDate getDate();
    int getDuration();

}
