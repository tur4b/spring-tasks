package org.example.cucumber.steps;

import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dao.TrainingRepository;
import org.example.dao.TrainingTypeRepository;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DatabaseHooks {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Before(order = 1)
    public void cleanDatabase() {
        // Delete trainings first (FK refs trainee, trainer, type)
        trainingRepository.deleteAll();
        // Delete trainers next — Hibernate cascade removes join table rows and trainer's user
        trainerRepository.deleteAll();
        // Delete trainees last — join table rows are already gone; cascade removes trainee's user
        traineeRepository.deleteAll();
        // TrainingType is static reference data: never deleted so IDs remain stable (id=1 stays CARDIO)
        log.debug("[Hooks] database cleared before scenario");
    }

    @Before(order = 10)
    public void seedTrainingTypes() {
        if (trainingTypeRepository.count() == 0) {
            TrainingType cardio = new TrainingType();
            cardio.setName(TrainingTypeName.CARDIO);
            trainingTypeRepository.save(cardio);

            TrainingType strength = new TrainingType();
            strength.setName(TrainingTypeName.STRENGTH);
            trainingTypeRepository.save(strength);
        }
    }
}