Feature: Training management

  Background:
    Given a trainer is registered with firstName "Coach" lastName "Fit" specializationId 1
    And a trainee is registered with firstName "Student" lastName "Lean"
    And the trainer is assigned to the trainee

  Scenario: Successfully create a training
    When I create a training with name "Morning Run" date "2030-01-15" duration 60
    Then the response status should be 200

  Scenario: Create training fails when date is in the past
    When I create a training with name "Old Run" date "2000-01-01" duration 60
    Then the response status should be 400

  Scenario: Create training fails when duration is zero
    When I create a training with name "Zero Run" date "2030-06-15" duration 0
    Then the response status should be 400

  Scenario: Create training requires authentication
    When I create a training without authentication name "Run" date "2030-01-15" duration 45
    Then the response status should be 403