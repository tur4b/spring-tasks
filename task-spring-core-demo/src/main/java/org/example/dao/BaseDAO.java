package org.example.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO layer abstraction
 * @param <Entity>
 * @param <ID>
 */
public interface BaseDAO<Entity, ID> {

    /**
     * Get Entity by ID
     *
     * @param id entity ID
     * @return Optional containing Entity
     */
    Optional<Entity> findById(ID id);

    /**
     * Retrieve Entity list
     *
     * @return list of Entity
     */
    List<Entity> findAll();

    /**
     * Create a new Entity
     *
     * @param entity Entity for save
     * @return created new Entity
     */
    Entity create(Entity entity);

    /**
     * Update existing Entity
     *
     * @param entity Entity for update
     * @return updated Entity
     */
    Entity update(Entity entity);

    /**
     * Delete Entity by ID
     *
     * @param id Entity ID
     * @return true if Entity deleted, false otherwise
     */
    boolean deleteById(ID id);

    /**
     * Check if Entity exists by ID
     *
     * @param id the id of Entity
     * @return true if Entity exists, false does not
     */
    boolean existsById(ID id);
}
