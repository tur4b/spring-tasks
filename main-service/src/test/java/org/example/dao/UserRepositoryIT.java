package org.example.dao;

import org.example.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("save and findByUsername returns the stored user")
    void saveAndFindByUsername_ReturnsStoredUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("encoded-password");

        userRepository.saveAndFlush(user);

        assertThat(userRepository.findByUsername("john.doe")).isPresent();
        assertThat(userRepository.existsByUsername("john.doe")).isTrue();
    }

    @Test
    @DisplayName("changePassword updates the stored password")
    void changePassword_UpdatesPassword() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("old-password");
        userRepository.saveAndFlush(user);

        int updatedRows = userRepository.changePassword("john.doe", "new-password");

        assertThat(updatedRows).isEqualTo(1);
        assertThat(userRepository.findByUsername("john.doe")).get().extracting(User::getPassword).isEqualTo("new-password");
    }
}

