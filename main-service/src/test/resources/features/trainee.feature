Feature: Trainee management

  Scenario: Successfully register a new trainee
    When I register a trainee with firstName "Alice" lastName "Smith"
    Then the response status should be 201
    And the response should contain a username
    And the response should contain a password

  Scenario: Registration fails when first name is blank
    When I register a trainee with firstName "" lastName "Smith"
    Then the response status should be 400

  Scenario: Registration fails when last name is too short
    When I register a trainee with firstName "Alice" lastName "AB"
    Then the response status should be 400

  Scenario: Get trainee profile after registration
    Given a trainee is registered with firstName "Bob" lastName "Brown"
    When I request the profile for the registered trainee
    Then the response status should be 200
    And the profile firstName is "Bob"
    And the profile lastName is "Brown"

  Scenario: Get trainee profile requires authentication
    When I request the profile for username "ghost.user" without a token
    Then the response status should be 403

  Scenario: Get profile for unknown trainee returns 404
    Given I am authenticated as a registered trainee with firstName "Carol" lastName "Jones"
    When I request the profile for username "no.such.user"
    Then the response status should be 404

  Scenario: Delete trainee by username
    Given a trainee is registered with firstName "Dan" lastName "Lee"
    When I delete the registered trainee
    Then the response status should be 204

  Scenario: Update trainee status to inactive
    Given a trainee is registered with firstName "Eve" lastName "White"
    When I deactivate the registered trainee
    Then the response status should be 200