package org.example.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDAO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.util.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryUserDAO implements UserDAO {

    private final Map<Long, User> users;
    private final IdGenerator idGenerator;

    public InMemoryUserDAO(@Qualifier("userMap") Map<Long, User> users,
                           IdGenerator idGenerator) {
        this.users = users;
        this.idGenerator = idGenerator;
    }

    /**
     * Get user by username
     *
     * @param username user username
     * @return Optional containing user, returns empty if not found
     */
    @Override
    public Optional<User> findByUsername(String username) {
        if(username == null || username.isBlank()){
            log.warn("Invalid username: {}", username);
            return Optional.empty();
        }
        return users.values()
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Check if user exists by username
     *
     * @param username user username
     * @return true if user exists by username, false if not exists
     */
    @Override
    public boolean existsByUsername(String username) {
        log.debug("Check existence of user by username: {}", username);
        return findByUsername(username).isPresent();
    }

    /**
     * Check if user exists by ID
     *
     * @param id user id
     * @return true if user exists by id, false if not exists
     */
    @Override
    public boolean existsById(Long id) {
        log.debug("Check existence of user by id: {}", id);
        return findById(id).isPresent();
    }

    /**
     * Get User by ID
     *
     * @param userId the id of the user
     * @return Optional containing user
     */
    @Override
    public Optional<User> findById(Long userId) {
        if(userId == null || userId < 0){
            log.warn("Invalid user ID: {}", userId);
            return Optional.empty();
        }
        User user = users.get(userId);
        log.debug("User fetched with ID: {}", user);
        return Optional.ofNullable(user);
    }

    /**
     * Retrieves all users
     * @return list of users
     */
    @Override
    public List<User> findAll() {
        return users.values()
                .stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Creates a new user in in-memory storage
     *
     * @param user is user to create
     * @return created user
     */
    @Override
    public User create(User user) {
        if(user == null) {
            log.error("Cannot create null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        Long userId = idGenerator.getNextId(User.class.getSimpleName());
        user.setId(userId);

        user.setActive(true);

        users.put(userId, user);
        log.debug("User created with id: {}", userId);

        return user;
    }

    /**
     * Updates an existing user in in-memory storage
     *
     * @param user is user to update
     * @return updated user
     */
    @Override
    public User update(User user) {
        if(user == null || user.getId() == null || user.getId() < 0){
            log.warn("Invalid user for update: {}", user);
            throw new IllegalArgumentException("Invalid user for update");
        }

        users.put(user.getId(), user);
        log.info("User updated with id: {}", user.getId());

        return user;
    }

    /**
     * Soft Deletes a user by ID
     *
     * @param userId is user's id
     * @return true if user was softly deleted, false if user with given ID was not found
     */
    @Override
    public boolean deleteById(Long userId) {
        User user = findById(userId)
                .orElse(null);

        boolean isDeleted = false;

        if(user == null) {
            log.info("User not found with id: {}", userId);
            isDeleted = false;
        } else {
            // soft delete
            user.setActive(false);
            user.setDeletedAt(LocalDateTime.now());
            isDeleted = true;
            log.info("User deleted with id: {}", userId);
        }
        return isDeleted;
    }
}
