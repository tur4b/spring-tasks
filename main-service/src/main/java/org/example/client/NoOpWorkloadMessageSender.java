package org.example.client;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "workload.messaging.enabled", havingValue = "false")
public class NoOpWorkloadMessageSender implements WorkloadMessageSender {

    @Override
    public void send(WorkloadEventRequest request) {
        log.debug("Workload messaging disabled; skipping send for trainer={}", request.trainerUsername());
    }
}
