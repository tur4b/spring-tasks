package org.example.client;

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

    private final WorkloadMessageSender workloadMessageSender;

    public void publishAdd(Training training, Trainer trainer) {
        submit(buildRequest(training, trainer, ActionType.ADD));
    }

    public void publishDelete(Training training, Trainer trainer) {
        submit(buildRequest(training, trainer, ActionType.DELETE));
    }

    private void submit(WorkloadEventRequest request) {
        log.info("[transactionId={}] OPERATION publishWorkload action={} trainer={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername());
        workloadMessageSender.send(request);
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
