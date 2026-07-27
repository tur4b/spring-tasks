package org.example.cucumber.steps;

import io.cucumber.java.Before;
import org.example.dao.TrainingTypeRepository;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseHooks {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

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