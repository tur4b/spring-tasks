package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.api.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final PasswordEncoder passwordEncoder;

    /**
     * Return application health status payload.
     *
     * @return status marker with a sample encoded value
     */
    @GetMapping
    public String health() {
        System.out.println("ok");
        log.info("this is a health endpoint");
        return "UP: " + passwordEncoder.encode("password");
    }
}
