package org.example.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "training_types")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "training_types_seq")
    @SequenceGenerator(
            name = "training_types_seq",
            sequenceName = "training_types_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private TrainingTypeName name;

}
