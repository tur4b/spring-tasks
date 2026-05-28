package org.example.dao;

import javax.validation.Valid;
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

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsById(Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.password = :password where u.username = :username")
    int changePassword(@Param("username") String username, @Param("password") String password);

}
