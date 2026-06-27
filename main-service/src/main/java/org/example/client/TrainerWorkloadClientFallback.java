package org.example.client;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainerWorkloadClientFallback implements TrainerWorkloadClient {

    @Override
    public void submitWorkload(WorkloadEventRequest request) {
        log.warn("[transactionId={}] OPERATION workloadFeignFallback action={} trainer={} reason=circuit-breaker-or-service-unavailable",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername());
    }
}
