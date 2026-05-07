package org.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;


/**
 * Main application entry point for the app
 * Spring configuration class for gym CRM application
 * Uses annotation-based approach for application context configuration
 * Scans the org.example package for components and loads properties from application.properties
 *
 * @author Turab
 */
@Configuration
@ComponentScan(basePackages = "org.example")
@PropertySource(value = "classpath:application.properties")
public class TaskSpringCoreDemoApplication {

    public static void main( String[] args ) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TaskSpringCoreDemoApplication.class);

        // you can delete this method safely
        someTestingOperations();
    }

    /**
     * can be deleted safely
     * this method is just for checking the app works correctly
     */
    private static void someTestingOperations() {
        // we can apply gym operations using GymOperationsFacade
//        GymOperationsFacade gymOperationsFacade = applicationContext.getBean(GymOperationsFacade.class);
//
//        gymOperationsFacade.createTrainee(
//                new TraineeCreateRequest("turab", "eybaliyev", "AZ-1", LocalDate.of(1998, Month.MAY, 18))
//        );
//
//        gymOperationsFacade.createTrainee(
//                new TraineeCreateRequest("turab", "eybaliyev", "AZ-2", LocalDate.of(1999, Month.APRIL, 5))
//        );
//
//        List<TraineeDTO> allTrainees = gymOperationsFacade.findAllTrainees();
//        System.out.println("All Trainees: " + allTrainees);
//
//        System.out.println("-------------------------------");
//
//        List<UserDTO> allUsers = gymOperationsFacade.findAllUsers();
//        System.out.println("All users: " + allUsers);
    }

}
