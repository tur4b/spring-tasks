package org.example.dao;

import org.example.entity.User;

import java.util.Optional;

/**
 * DAO layer abstraction for user operations
 */
public interface UserDAO extends BaseDAO<User, Long> {

    /**
     * Retrieves a user by username.
     *
     * @param username user username
     * @return Optional containing the user if found, otherwise empty
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if user exists by username
     *
     * @param username user username
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if user exists by id
     *
     * @param id the id of the user
     * @return true if user exists, false otherwise
     */
    boolean existsById(Long id);
}
