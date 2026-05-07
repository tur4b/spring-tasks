package org.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Training {

    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String name;
    private TrainingType type;
    private LocalDate date;
    private int duration; // let's consider duration with minutes

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Training() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Training{" +
                "id=" + id +
                ", traineeId=" + traineeId +
                ", trainerId=" + trainerId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", date=" + date +
                ", duration=" + duration +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
