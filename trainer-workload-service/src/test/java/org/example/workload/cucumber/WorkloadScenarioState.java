package org.example.workload.cucumber;

import io.cucumber.spring.ScenarioScope;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@ScenarioScope
public class WorkloadScenarioState {
    private int lastStatus;
    private String lastResponseBody;
    private String jwtToken;
}