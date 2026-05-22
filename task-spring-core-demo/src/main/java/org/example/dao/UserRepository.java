package org.example.dao;

import jakarta.validation.Valid;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * JPA repository for User class
 */
public interface UserRepository extends JpaRepository<User, Long> {

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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.password = :password where u.username = :username")
    int changePassword(@Param("username") String username, @Param("password") String password);

}
