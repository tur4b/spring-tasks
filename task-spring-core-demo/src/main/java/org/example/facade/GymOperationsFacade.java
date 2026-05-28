//package org.example.facade;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.example.dao.projection.TrainerView;
//import org.example.dao.projection.TraineeView;
//import org.example.dao.projection.TrainingView;
//import org.example.dto.request.*;
//import org.example.dto.response.TraineeDTO;
//import org.example.dto.response.TrainerDTO;
//import org.example.dto.response.TrainingDTO;
//import org.example.dto.response.UserDTO;
//import org.example.service.api.*;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.function.Supplier;
//
///**
// * Facade class that provides a unified interface to the Gym services.
// * Delegates all operations to the appropriate service beans.
// */
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class GymOperationsFacade {
//
//    private final TraineeService traineeService;
//    private final TrainerService trainerService;
//    private final TrainingService trainingService;
//    private final UserService userService;
//
//    private static final AuthRequest authRequest = new AuthRequest("trainee.admin", "admin123");
//
//    // --- UserService examples ---
//    public List<UserDTO> exampleUserFindAll() {
//        return userService.findAll(authRequest);
//    }
//
//    public UserDTO exampleUserFindById() {
//        return userService.findById(1L, authRequest);
//    }
//
//    public UserDTO exampleUserFindByUsername() {
//        return userService.findByUsername("trainee.admin", authRequest);
//    }
//
//    public UserDTO exampleUserCreate() {
//        return userService.createUser(new UserCreateRequest("Alice", "Walker"));
//    }
//
//    public UserDTO exampleUserUpdate() {
//        return userService.updateUser(3L, new UserUpdateRequest("Updated", "User"), authRequest);
//    }
//
//    public boolean exampleUserExistsById() {
//        return userService.existsById(1L);
//    }
//
//    public void exampleUserChangePassword() {
//        userService.changePassword(new ChangePasswordRequest("hero.heros", "newPass123"), authRequest);
//    }
//
//    // --- TraineeService examples ---
//    public List<TraineeView> exampleTraineeFindAllView() {
//        return traineeService.findAllTraineesView(authRequest);
//    }
//
//    public TraineeView exampleTraineeFindViewById() {
//        return traineeService.findTraineeViewById(1L, authRequest);
//    }
//
//    public TraineeDTO exampleTraineeCreate() {
//        return traineeService.createTrainee(new TraineeCreateRequest(
//                "Brian",
//                "Mills",
//                "Baku",
//                LocalDate.of(1997, 6, 15)
//        ));
//    }
//
//    public TraineeDTO exampleTraineeUpdate() {
//        return traineeService.updateTrainee(2L, new TraineeUpdateRequest(
//                "Mills",
//                "Brian",
//                "Ganja",
//                LocalDate.of(1997, 6, 15)
//        ), authRequest);
//    }
//
//    public boolean exampleTraineeDeleteByUsername() {
//        return traineeService.deleteTraineeByUsername("brian.mills", authRequest);
//    }
//
//    public boolean exampleTraineeExistsById() {
//        return traineeService.existsById(1L);
//    }
//
//    public void exampleTraineeActivate() {
//        traineeService.activate(2L, authRequest);
//    }
//
//    public void exampleTraineeDeactivate() {
//        traineeService.deactivate(1L, authRequest);
//    }
//
//    public void exampleTraineeChangePassword() {
//        traineeService.changePassword(new ChangePasswordRequest("hero.heros", "1231231"), authRequest);
//    }
//
//    public List<TrainingView> exampleTraineeTrainingsByCriteria() {
//        return traineeService.findTrainingsOfTraineeByCriteria(
//                new TrainingsOfTraineeSearchCriteria(
//                        "trainee.admin",
//                        LocalDate.now().minusMonths(1),
//                        LocalDate.now().plusMonths(1),
//                        "trainer",
//                        1
//                ),
//                authRequest
//        );
//    }
//
//    // --- TrainerService examples ---
//    public List<TrainerView> exampleTrainerFindAllView() {
//        return trainerService.findAllTrainersView(authRequest);
//    }
//
//    public TrainerView exampleTrainerFindViewById() {
//        return trainerService.findTrainerViewById(1L, authRequest);
//    }
//
//    public TrainerDTO exampleTrainerCreate() {
//        return trainerService.createTrainer(new TrainerCreateRequest("John", "Stone", 1));
//    }
//
//    public TrainerDTO exampleTrainerUpdate() {
//        return trainerService.updateTrainer(1L, new TrainerUpdateRequest("John", "Updated", 1), authRequest);
//    }
//
//    public boolean exampleTrainerExistsById() {
//        return trainerService.existsById(1L);
//    }
//
//    public void exampleTrainerActivate() {
//        trainerService.activate(1L, authRequest);
//    }
//
//    public void exampleTrainerDeactivate() {
//        trainerService.deactivate(1L, authRequest);
//    }
//
//    public boolean exampleTrainerTraineeRelationExists() {
//        return trainerService.existsTrainerTraineeRelation(1L, 1L);
//    }
//
//    public void exampleTrainerChangePassword() {
//        trainerService.changePassword(new ChangePasswordRequest("trainer-1.trainerrrrr-1", "newPass123"), authRequest);
//    }
//
//    public List<TrainingView> exampleTrainerTrainingsByCriteria() {
//        return trainerService.findTrainingsOfTrainerByCriteria(
//                new TrainingsOfTrainerSearchCriteria(
//                        "trainer-1.trainerrrrr-1",
//                        LocalDate.now().minusMonths(1),
//                        LocalDate.now().plusMonths(1),
//                        "brian",
//                        1
//                ),
//                authRequest
//        );
//    }
//
//    public List<TrainerView> exampleFindTrainersNotAssignedToTrainee() {
//        return trainerService.findTrainersNotAssignedToTrainee("trainee.admin", authRequest);
//    }
//
//    public void exampleReassignTraineeToTrainers() {
//        trainerService.reassignTraineeToTrainers(1L, List.of(1L, 2L), authRequest);
//    }
//
//    // --- TrainingService examples ---
//    public List<TrainingView> exampleTrainingFindAllView() {
//        return trainingService.findAllTrainingsView(authRequest);
//    }
//
//    public TrainingView exampleTrainingFindViewById() {
//        return trainingService.findTrainingViewById(1L, authRequest);
//    }
//
//    public TrainingDTO exampleTrainingCreate() {
//        return trainingService.createTraining(new TrainingCreateRequest(
//                1L,
//                1L,
//                "Morning Session",
//                1,
//                LocalDate.now().plusDays(1),
//                60
//        ), authRequest);
//    }
//
//    public TrainingDTO exampleTrainingUpdate() {
//        return trainingService.updateTraining(1L, new TrainingUpdateRequest(
//                1L,
//                1L,
//                "Evening Session",
//                1,
//                LocalDate.now().plusDays(2),
//                75
//        ), authRequest);
//    }
//
//    public boolean exampleTrainingDelete() {
//        return trainingService.deleteTraining(1L, authRequest);
//    }
//
//    // Executes every example method and logs result or failure without stopping the run.
//    public void runAllExamples() {
//        log.info(">>> Running UserService examples");
//        run("exampleUserFindAll", this::exampleUserFindAll);
//        run("exampleUserFindById", this::exampleUserFindById);
//        run("exampleUserFindByUsername", this::exampleUserFindByUsername);
//        run("exampleUserCreate", this::exampleUserCreate);
//        run("exampleUserUpdate", this::exampleUserUpdate);
//        run("exampleUserExistsById", this::exampleUserExistsById);
//        runVoid("exampleUserChangePassword", this::exampleUserChangePassword);
//
//        log.info(">>> Running TraineeService examples");
//        run("exampleTraineeFindAllView", this::exampleTraineeFindAllView);
//        run("exampleTraineeFindViewById", this::exampleTraineeFindViewById);
//        run("exampleTraineeCreate", this::exampleTraineeCreate);
//        run("exampleTraineeUpdate", this::exampleTraineeUpdate);
//        run("exampleTraineeDeleteByUsername", this::exampleTraineeDeleteByUsername);
//        run("exampleTraineeExistsById", this::exampleTraineeExistsById);
//        runVoid("exampleTraineeActivate", this::exampleTraineeActivate);
//        runVoid("exampleTraineeDeactivate", this::exampleTraineeDeactivate);
//        runVoid("exampleTraineeChangePassword", this::exampleTraineeChangePassword);
//        run("exampleTraineeTrainingsByCriteria", this::exampleTraineeTrainingsByCriteria);
//
//        log.info(">>> Running TrainerService examples");
//        run("exampleTrainerFindAllView", this::exampleTrainerFindAllView);
//        run("exampleTrainerFindViewById", this::exampleTrainerFindViewById);
//        run("exampleTrainerCreate", this::exampleTrainerCreate);
//        run("exampleTrainerUpdate", this::exampleTrainerUpdate);
//        run("exampleTrainerExistsById", this::exampleTrainerExistsById);
//        runVoid("exampleTrainerActivate", this::exampleTrainerActivate);
//        runVoid("exampleTrainerDeactivate", this::exampleTrainerDeactivate);
//        run("exampleTrainerTraineeRelationExists", this::exampleTrainerTraineeRelationExists);
//        runVoid("exampleTrainerChangePassword", this::exampleTrainerChangePassword);
//        run("exampleTrainerTrainingsByCriteria", this::exampleTrainerTrainingsByCriteria);
//        run("exampleFindTrainersNotAssignedToTrainee", this::exampleFindTrainersNotAssignedToTrainee);
//        runVoid("exampleReassignTraineeToTrainers", this::exampleReassignTraineeToTrainers);
//
//        log.info(">>> Running TrainingService examples");
//        run("exampleTrainingFindAllView", this::exampleTrainingFindAllView);
//        run("exampleTrainingFindViewById", this::exampleTrainingFindViewById);
//        run("exampleTrainingCreate", this::exampleTrainingCreate);
//        run("exampleTrainingUpdate", this::exampleTrainingUpdate);
//        run("exampleTrainingDelete", this::exampleTrainingDelete);
//    }
//
//    private void runVoid(String operation, Runnable action) {
//        try {
//            action.run();
//            log.info("{} -> OK", operation);
//        } catch (Exception ex) {
//            log.error("{} -> FAILED: {}", operation, ex.getMessage(), ex);
//        }
//    }
//
//    private void run(String operation, Supplier<?> action) {
//        try {
//            Object result = action.get();
//            log.info("{} -> OK, result: {}", operation, result);
//        } catch (Exception ex) {
//            log.error("{} -> FAILED: {}", operation, ex.getMessage(), ex);
//        }
//    }
//}
//
