package org.example.client;

import org.example.common.dto.WorkloadEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "trainer-workload-service",
        fallback = TrainerWorkloadClientFallback.class,
        configuration = FeignClientConfig.class
)
public interface TrainerWorkloadClient {

    @PostMapping("/workloads")
    void submitWorkload(@RequestBody WorkloadEventRequest request);
}
