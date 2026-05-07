package org.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Trainee {
    private Long id;
    private Long userId;
    private String address;
    private LocalDate dateOfBirth;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Trainee() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "id=" + id +
                ", userId=" + userId +
                ", address='" + address + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
