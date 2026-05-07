package org.example.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataInitializationPostProcessor implements BeanPostProcessor {

    private final Resource initialDataResource;
    private final ObjectMapper objectMapper;

    public DataInitializationPostProcessor(@Value("${storage.data.path}") Resource initialDataResource,
                                           ObjectMapper objectMapper) {
        this.initialDataResource = initialDataResource;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        log.info(">> {} postProcessAfterInitialization(beanName: {})", this.getClass().getSimpleName(), beanName);

        if (bean instanceof Map) {
            loadEntityData((Map<Long, Object>) bean, beanName);
        }
        return bean;
    }

    private void loadEntityData(Map<Long, Object> map, String beanName) {
        try {
            PreInitializedData preInitializedData = objectMapper.readValue(initialDataResource.getInputStream(), PreInitializedData.class);
            if("userMap".equals(beanName)){
                List<User> users = preInitializedData.users();
                for(User user : users){
                    map.put(user.getId(), user);
                }
            } else if("traineeMap".equals(beanName)){
                List<Trainee> trainees = preInitializedData.trainees();
                for(Trainee trainee : trainees){
                    map.put(trainee.getId(), trainee);
                }
            } else if("trainerMap".equals(beanName)){
                List<Trainer> trainers = preInitializedData.trainers();
                for(Trainer trainer : trainers){
                    map.put(trainer.getId(), trainer);
                }
            } else if("trainingMap".equals(beanName)){
                List<Training> trainings = preInitializedData.trainings();
                for(Training training : trainings){
                    map.put(training.getId(), training);
                }
            }

            log.info(">> data: {}", map.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record PreInitializedData(
            List<User> users,
            List<Trainee> trainees,
            List<Trainer> trainers,
            List<Training> trainings
    ) {}
}

