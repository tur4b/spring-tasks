package org.example.controller;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.BaseResponse;
import org.example.service.api.TrainingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trainings")
@Api(tags = "Trainings")
public class TrainingController {

    private final TrainingService trainingService;

    /**
     * Create a new training entry.
     *
     * @param createRequest validated training creation payload
     * @return OK when training is created successfully
     */
    @PostMapping
    @ApiOperation(value = "Create training")
    public ResponseEntity<BaseResponse<?>> createTraining(@Valid @RequestBody TrainingCreateRequest createRequest) {

        trainingService.createTraining(createRequest);
        return ResponseEntity.ok().build();
    }
}
