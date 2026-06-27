package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.model.ActionType;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.User;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadPublisher {

    private final TrainerWorkloadClient trainerWorkloadClient;

    @CircuitBreaker(name = "trainerWorkload", fallbackMethod = "publishAddFallback")
    public void publishAdd(Training training, Trainer trainer) {
        submit(buildRequest(training, trainer, ActionType.ADD));
    }

    @CircuitBreaker(name = "trainerWorkload", fallbackMethod = "publishDeleteFallback")
    public void publishDelete(Training training, Trainer trainer) {
        submit(buildRequest(training, trainer, ActionType.DELETE));
    }

    private void submit(WorkloadEventRequest request) {
        log.info("[transactionId={}] OPERATION publishWorkload action={} trainer={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername());
        trainerWorkloadClient.submitWorkload(request);
    }

    private void publishAddFallback(Training training, Trainer trainer, Throwable throwable) {
        logFallback(buildRequest(training, trainer, ActionType.ADD), throwable);
    }

    private void publishDeleteFallback(Training training, Trainer trainer, Throwable throwable) {
        logFallback(buildRequest(training, trainer, ActionType.DELETE), throwable);
    }

    private void logFallback(WorkloadEventRequest request, Throwable throwable) {
        log.warn("[transactionId={}] OPERATION workloadCircuitBreakerFallback action={} trainer={} error={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername(),
                throwable.getMessage());
    }

    private WorkloadEventRequest buildRequest(Training training, Trainer trainer, ActionType actionType) {
        User user = trainer.getUser();
        return new WorkloadEventRequest(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.isActive(),
                training.getDate(),
                training.getDuration(),
                actionType
        );
    }
}
