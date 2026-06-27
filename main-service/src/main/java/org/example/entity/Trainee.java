package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainees_seq")
    @SequenceGenerator(
            name = "trainees_seq",
            sequenceName = "trainees_id_seq",
            allocationSize = 1
    )
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    private String address;

    private LocalDate dateOfBirth;

    @ManyToMany(mappedBy = "trainees", fetch = FetchType.LAZY)
    private List<Trainer> trainers = new ArrayList<>();

    // Cascade hard-delete of trainings when trainee is deleted
    @OneToMany(
            mappedBy = "trainee",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Training> trainings = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
