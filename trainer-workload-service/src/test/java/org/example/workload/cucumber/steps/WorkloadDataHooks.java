package org.example.workload.cucumber.steps;

import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.repository.TrainerSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WorkloadDataHooks {

    @Autowired
    private TrainerSummaryRepository repository;

    @Before(order = 1)
    public void cleanDatabase() {
        repository.deleteAll();
        log.debug("[Hooks] trainer_summaries collection cleared before scenario");
    }
}