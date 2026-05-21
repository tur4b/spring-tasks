package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.facade.GymOperationsFacade;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;


/**
 * Main application entry point for the app
 * Spring configuration class for gym CRM application
 * Uses annotation-based approach for application context configuration
 * Scans the {@code org.example} package for components and loads properties from application.properties
 *
 * @author Turab
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "org.example")
@PropertySource(value = "classpath:application.properties")
@EnableAspectJAutoProxy
public class TaskSpringCoreDemoApplication {

    public static void main( String[] args ) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TaskSpringCoreDemoApplication.class);

        // you can delete this method safely
        someTestingOperations(applicationContext);
    }

    /**
     * can be deleted safely
     * this method is just for checking the app works correctly
     */
    private static void someTestingOperations(ApplicationContext applicationContext) {
        // we can apply gym operations using GymOperationsFacade
        GymOperationsFacade gymOperationsFacade = applicationContext.getBean(GymOperationsFacade.class);
        gymOperationsFacade.runAllExamples();
    }

}
