package org.example.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.TrainerDAO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.util.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO layer implementation for trainer operations
 * Uses in memory storage to store data
 */
@Slf4j
@Repository
public class InMemoryTrainerDAO implements TrainerDAO {

    private final Map<Long, Trainer> trainerMap;
    private final IdGenerator idGenerator;

    /**
     * Constructor for <code>InMemoryTrainerDAO</code>
     * constructor injection applied for required dependencies
     *
     * @param trainerMap Map<Long, Trainer> instance
     * @param idGenerator IdGeneratorService instance
     */
    public InMemoryTrainerDAO(@Qualifier("trainerMap") Map<Long, Trainer> trainerMap, 
                              IdGenerator idGenerator) {
        this.trainerMap = trainerMap;
        this.idGenerator = idGenerator;
    }

    /**
     * Get trainer by ID
     *
     * @param trainerId trainer's ID
     * @return Optional containing trainer, returns empty if not found
     */
    @Override
    public Optional<Trainer> findById(Long trainerId) {
        if(trainerId == null || trainerId < 0){
            log.warn("Invalid trainer ID: {}", trainerId);
            return Optional.empty();
        }
        Trainer trainer = trainerMap.get(trainerId);
        log.debug("Trainer fetched with ID: {}", trainerId);
        return Optional.ofNullable(trainer);
    }


    /**
     * Retrieves all trainers
     * @return list of trainers
     */
    @Override
    public List<Trainer> findAll() {
        return trainerMap.values()
                .stream()
                .filter(Trainer::isActive)
                .toList();
    }

    /**
     * Creates a new trainer in in-memory storage
     *
     * @param trainer is trainer to create
     * @return created trainer
     */
    @Override
    public Trainer create(Trainer trainer) {
        if(trainer == null){
            log.error("Cannot create null trainer");
            throw new IllegalArgumentException("Trainer cannot be null");
        }
        Long nextTrainerId = idGenerator.getNextId(Trainer.class.getSimpleName());
        trainer.setId(nextTrainerId);

        trainerMap.put(nextTrainerId, trainer);
        log.info("Trainer created with ID: {}", nextTrainerId);

        return trainer;
    }

    /**
     * Updates an existing trainer in in-memory storage
     *
     * @param trainer is trainer to update
     * @return updated trainer
     */
    @Override
    public Trainer update(Trainer trainer) {
        if(trainer == null || trainer.getId() == null || trainer.getId() < 0){
            log.warn("Invalid trainer for update");
            throw new IllegalArgumentException("Invalid trainer for update");
        }
        if (!trainerMap.containsKey(trainer.getId())) {
            log.error("Trainer not found with ID: {}", trainer.getId());
            throw new RuntimeException("Trainer not found with ID: " + trainer.getId());
        }

        trainerMap.put(trainer.getId(), trainer);
        log.info("Trainer updated with ID: {}", trainer.getId());

        return trainer;
    }

    /**
     * Soft Deletes a trainer by ID
     *
     * @param trainerId is trainer's id
     * @return true if trainer was deleted softly, false if trainer with given ID was not found
     */
    @Override
    public boolean deleteById(Long trainerId) {
        Trainer trainer = findById(trainerId)
                .orElse(null);

        boolean isDeleted = false;

        if(trainer == null){
            log.info("Trainer not found with id: {}", trainerId);
            isDeleted = false;
        } else {
            trainer.setActive(false);
            trainer.setDeletedAt(LocalDateTime.now());
            isDeleted = true;
            log.info("Trainer deleted with ID: {}", trainerId);
        }
        return isDeleted;
    }

    /**
     * Check if trainer exists by ID
     *
     * @param id trainer id
     * @return true if trainer exists by id, false if not exists
     */
    @Override
    public boolean existsById(Long id) {
        log.debug("Check existence of trainer by id: {}", id);
        return findById(id).isPresent();
    }

}
