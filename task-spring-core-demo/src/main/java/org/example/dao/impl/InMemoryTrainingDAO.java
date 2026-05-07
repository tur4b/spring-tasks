package org.example.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TrainingDAO;
import org.example.entity.Training;
import org.example.util.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO layer implementation for training operations
 * Uses in memory storage to store data
 */
@Slf4j
@Repository
public class InMemoryTrainingDAO implements TrainingDAO {

    private final Map<Long, Training> trainingMap;
    private final IdGenerator idGenerator;

    /**
     * Constructor for <code>InMemoryTrainingDAO</code>
     * constructor injection applied for required dependencies
     *
     * @param trainingMap Map<Long, Training> instance
     * @param idGenerator IdGeneratorService instance
     */
    public InMemoryTrainingDAO(@Qualifier("trainingMap") Map<Long, Training> trainingMap,
                               IdGenerator idGenerator) {
        this.trainingMap = trainingMap;
        this.idGenerator = idGenerator;
    }

    /**
     * Get training by ID
     *
     * @param trainingId training's ID
     * @return Optional containing training, returns empty if not found
     */
    @Override
    public Optional<Training> findById(Long trainingId) {
        if(trainingId == null || trainingId < 0){
            log.warn("Invalid training ID: {}", trainingId);
            return Optional.empty();
        }
        Training training = trainingMap.get(trainingId);
        log.debug("Training fetched with ID: {}", trainingId);
        return Optional.ofNullable(training);
    }


    /**
     * Retrieves all trainings
     * @return list of trainings
     */
    @Override
    public List<Training> findAll() {
        return trainingMap.values()
                .stream()
                .filter(Training::isActive)
                .toList();
    }

    /**
     * Creates a new training in in-memory storage
     *
     * @param training is training to create
     * @return created training
     */
    @Override
    public Training create(Training training) {
        if(training == null){
            log.error("Cannot create null training");
            throw new IllegalArgumentException("Training cannot be null");
        }
        // set id to training
        Long nextTrainingId = idGenerator.getNextId(Training.class.getSimpleName());
        training.setId(nextTrainingId);

        trainingMap.put(nextTrainingId, training);
        log.info("Training created with ID: {}", nextTrainingId);

        return training;
    }

    /**
     * Updates an existing training in in-memory storage
     *
     * @param training is training to update
     * @return updated training
     */
    @Override
    public Training update(Training training) {
        if(training == null){
            log.warn("Invalid training for update");
            throw new IllegalArgumentException("Invalid training for update");
        }
        if (!trainingMap.containsKey(training.getId())) {
            log.error("Training not found with ID: {}", training.getId());
            throw new RuntimeException("Training not found with ID: " + training.getId());
        }

        trainingMap.put(training.getId(), training);
        log.info("Training updated with ID: {}", training.getId());

        return training;
    }

    /**
     * Soft Deletes a training by ID
     *
     * @param trainingId is training's id
     * @return true if training was deleted, false if training with given ID was not found
     */
    @Override
    public boolean deleteById(Long trainingId) {
        Training training = findById(trainingId)
                .orElse(null);

        boolean isDeleted = false;

        if(training == null){
            log.info("Training not found with id: {}", trainingId);
            isDeleted = false;
        } else {
            training.setActive(false);
            training.setDeletedAt(LocalDateTime.now());
            isDeleted = true;
            log.info("Training deleted with ID: {}", trainingId);
        }
        return isDeleted;
    }

    /**
     * Check if training exists by ID
     *
     * @param id training id
     * @return true if training exists by id, false if not exists
     */
    @Override
    public boolean existsById(Long id) {
        log.debug("Check existence of training by id: {}", id);
        return findById(id).isPresent();
    }

}
