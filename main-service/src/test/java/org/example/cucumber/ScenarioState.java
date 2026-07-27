package org.example.cucumber;

import io.cucumber.spring.ScenarioScope;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Holds per-scenario state shared across step-definition classes.
 */
@Data
@Component
@ScenarioScope
public class ScenarioState {
    private int lastStatus;
    private String lastResponseBody;
    private String registeredUsername;
    private String registeredPassword;
    private String jwtToken;
    private String registeredTrainerUsername;
    private String registeredTraineeUsername;
}