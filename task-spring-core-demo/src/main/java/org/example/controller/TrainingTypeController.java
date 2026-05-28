package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.BaseResponse;
import org.example.service.api.TrainingTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/training-types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    /**
     * Fetch all training types.
     *
     * @return list of available training types
     */
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getAllTrainingTypes() {
        return ResponseEntity.ok(
                new BaseResponse<>(
                        trainingTypeService.findAll(),
                        "TrainingType list"
                )
        );
    }
}
