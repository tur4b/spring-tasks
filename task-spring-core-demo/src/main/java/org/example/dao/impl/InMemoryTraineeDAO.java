package org.example.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeDAO;
import org.example.entity.Trainee;
import org.example.util.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO layer implementation for trainee operations
 * Uses in memory storage to store data
 */
@Slf4j
@Repository
public class InMemoryTraineeDAO implements TraineeDAO {

    private final Map<Long, Trainee> traineeMap;
    private final IdGenerator idGenerator;

    /**
     * Constructor for <code>InMemoryTraineeDAO</code>
     * constructor injection applied for required dependencies
     *
     * @param traineeMap Map<Long, Trainee> instance
     */
    public InMemoryTraineeDAO(@Qualifier("traineeMap") Map<Long, Trainee> traineeMap, IdGenerator idGenerator) {
        this.traineeMap = traineeMap;
        this.idGenerator = idGenerator;
    }

    /**
     * Get trainee by ID
     *
     * @param traineeId trainee's ID
     * @return Optional containing trainee, returns empty if not found
     */
    @Override
    public Optional<Trainee> findById(Long traineeId) {
        if(traineeId == null || traineeId < 0){
            log.warn("Invalid trainee ID: {}", traineeId);
            return Optional.empty();
        }
        Trainee trainee = traineeMap.get(traineeId);
        log.debug("Trainee fetched with ID: {}", traineeId);
        return Optional.ofNullable(trainee);
    }


    /**
     * Retrieves all trainees
     * @return list of trainees
     */
    @Override
    public List<Trainee> findAll() {
        return traineeMap.values()
                .stream()
                .filter(Trainee::isActive)
                .toList();
    }

    /**
     * Creates a new trainee in in-memory storage
     *
     * @param trainee is trainee to create
     * @return created trainee
     */
    @Override
    public Trainee create(Trainee trainee) {
        if(trainee == null){
            log.error("Cannot create null trainee");
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        Long nextTraineeId = idGenerator.getNextId(Trainee.class.getSimpleName());
        trainee.setId(nextTraineeId);

        traineeMap.put(nextTraineeId, trainee);
        log.info("Trainee created with ID: {}", nextTraineeId);

        return trainee;
    }

    /**
     * Updates an existing trainee in in-memory storage
     *
     * @param trainee is trainee to update
     * @return updated trainee
     */
    @Override
    public Trainee update(Trainee trainee) {
        if(trainee == null || trainee.getId() == null || trainee.getId() < 0){
            log.warn("Invalid trainee for update: {}", trainee);
            throw new IllegalArgumentException("Invalid trainee for update");
        }
        if (!traineeMap.containsKey(trainee.getId())) {
            log.error("Trainee not found with ID: {}", trainee.getId());
            throw new RuntimeException("Trainee not found with ID: " + trainee.getId());
        }

        traineeMap.put(trainee.getId(), trainee);
        log.info("Trainee updated with ID: {}", trainee.getId());

        return trainee;
    }

    /**
     * Soft Delete a trainee by ID
     *
     * @param traineeId is trainee's id
     * @return true if trainee was deleted softly, false if trainee with given ID was not found
     */
    @Override
    public boolean deleteById(Long traineeId) {
        Trainee trainee = findById(traineeId)
                .orElse(null);

        boolean isDeleted = false;

        if(trainee == null){
            log.info("Trainee not found with id: {}", traineeId);
            isDeleted = false;
        } else {
            trainee.setActive(false);
            trainee.setDeletedAt(LocalDateTime.now());
            isDeleted = true;
            log.info("Trainee deleted with ID: {}", traineeId);
        }
        return isDeleted;
    }

    /**
     * Check if trainee exists by ID
     *
     * @param id trainee id
     * @return true if trainee exists by id, false if not exists
     */
    @Override
    public boolean existsById(Long id) {
        log.debug("Check existence of trainee by id: {}", id);
        return findById(id).isPresent();
    }

}
