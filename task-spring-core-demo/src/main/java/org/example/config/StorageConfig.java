package org.example.config;

import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * General class to create storage components
 */
@Configuration
public class StorageConfig {

    @Bean(name = "userMap")
    public Map<Long, User> userMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "traineeMap")
    public Map<Long, Trainee> traineeMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "trainerMap")
    public Map<Long, Trainer> trainerMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "trainingMap")
    public Map<Long, Training> trainingMap() {
        return new ConcurrentHashMap<>();
    }
}
