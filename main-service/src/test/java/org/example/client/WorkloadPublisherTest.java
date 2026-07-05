package org.example.client;

import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadPublisher Unit Tests")
class WorkloadPublisherTest {

    @Mock
    private WorkloadMessageSender workloadMessageSender;

    @InjectMocks
    private WorkloadPublisher workloadPublisher;

    @Test
    @DisplayName("publishAdd delegates to JMS sender")
    void publishAdd_DelegatesToSender() {
        workloadPublisher.publishAdd(training(), trainer());

        verify(workloadMessageSender).send(any());
    }

    @Test
    @DisplayName("publishDelete delegates to JMS sender")
    void publishDelete_DelegatesToSender() {
        workloadPublisher.publishDelete(training(), trainer());

        verify(workloadMessageSender).send(any());
    }

    private Training training() {
        Training training = new Training();
        training.setDate(LocalDate.of(2026, 6, 15));
        training.setDuration(60);
        return training;
    }

    private Trainer trainer() {
        User user = new User();
        user.setUsername("trainer.one");
        user.setFirstName("John");
        user.setLastName("Smith");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setActive(true);
        return trainer;
    }
}
