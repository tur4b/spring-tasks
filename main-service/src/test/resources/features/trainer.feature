Feature: Trainer management

  Scenario: Successfully register a new trainer
    When I register a trainer with firstName "John" lastName "Smith" specializationId 1
    Then the response status should be 201
    And the response should contain a username
    And the response should contain a password

  Scenario: Registration fails when specialization id is missing
    When I register a trainer with firstName "John" lastName "Smith" without specialization
    Then the response status should be 400

  Scenario: Get trainer profile after registration
    Given a trainer is registered with firstName "Jane" lastName "Doe" specializationId 1
    When I request the trainer profile for the registered trainer
    Then the response status should be 200
    And the trainer profile firstName is "Jane"
    And the trainer profile lastName is "Doe"

  Scenario: Get trainer profile requires authentication
    When I request the trainer profile for username "ghost.trainer" without a token
    Then the response status should be 403

  Scenario: Get profile for unknown trainer returns 404
    Given I am authenticated as a registered trainer with firstName "Tom" lastName "Green" specializationId 1
    When I request the trainer profile for username "no.such.trainer"
    Then the response status should be 404

  Scenario: Update trainer status to inactive
    Given a trainer is registered with firstName "Lisa" lastName "Ray" specializationId 1
    When I deactivate the registered trainer
    Then the response status should be 200

  Scenario: Get trainers not assigned to trainee
    Given a trainer is registered with firstName "Mike" lastName "Fox" specializationId 1
    And a trainee is registered with firstName "Sara" lastName "Hill"
    When I request trainers not assigned to the registered trainee
    Then the response status should be 200