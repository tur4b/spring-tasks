Feature: Authentication

  Scenario: Login with valid credentials returns JWT token
    Given a trainee is registered with firstName "Auth" lastName "User"
    When I login with the registered trainee credentials
    Then the response status should be 200
    And the response should contain an access token

  Scenario: Login with wrong password returns 401
    Given a trainee is registered with firstName "Wrong" lastName "Pass"
    When I login with username and password "totally-wrong-password"
    Then the response status should be 401

  Scenario: Login with unknown username returns 401
    When I login with username "no.such.person" and password "password"
    Then the response status should be 401

  Scenario: Change password with valid JWT succeeds
    Given a trainee is registered with firstName "Chng" lastName "Pwd"
    And I login with the registered trainee credentials
    When I change the password to "newSecret123"
    Then the response status should be 200

  Scenario: Change password without JWT returns 403
    When I change the password for "some.user" oldPassword "old" newPassword "new" without token
    Then the response status should be 403

  Scenario: Logout invalidates the JWT token
    Given a trainee is registered with firstName "Logi" lastName "Out"
    And I login with the registered trainee credentials
    When I logout with the current token
    Then the response status should be 200
    When I logout with the current token again
    Then the response status should be 403