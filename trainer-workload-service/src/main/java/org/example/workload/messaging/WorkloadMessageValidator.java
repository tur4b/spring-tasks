package org.example.workload.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.common.dto.WorkloadEventRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkloadMessageValidator {

    private final Validator validator;

    public boolean isValid(WorkloadEventRequest request) {
        return validator.validate(request).isEmpty();
    }

    public String validationErrors(WorkloadEventRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }
}
