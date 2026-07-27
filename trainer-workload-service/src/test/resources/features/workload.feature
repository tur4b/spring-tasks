Feature: Trainer workload management

  Background:
    Given a valid JWT token for trainer-workload-service tests

  Scenario: Add workload entry and retrieve monthly summary
    When I add a workload for trainer "john.smith" firstName "John" lastName "Smith" date "2026-06-15" duration 60
    Then the workload response status should be 201
    When I query monthly workload for trainer "john.smith" year 2026 month 6
    Then the workload response status should be 200
    And the monthly duration should be 60

  Scenario: Add multiple workload entries accumulates duration
    When I add a workload for trainer "acc.trainer" firstName "Acc" lastName "Trainer" date "2026-03-10" duration 45
    And I add a workload for trainer "acc.trainer" firstName "Acc" lastName "Trainer" date "2026-03-20" duration 30
    When I query monthly workload for trainer "acc.trainer" year 2026 month 3
    Then the workload response status should be 200
    And the monthly duration should be 75

  Scenario: Delete workload entry reduces duration
    Given a workload exists for trainer "del.trainer" firstName "Del" lastName "Trainer" date "2026-06-15" duration 90
    When I delete workload for trainer "del.trainer" firstName "Del" lastName "Trainer" date "2026-06-15" duration 30
    Then the workload response status should be 204
    When I query monthly workload for trainer "del.trainer" year 2026 month 6
    Then the workload response status should be 200
    And the monthly duration should be 60

  Scenario: Delete more than available duration clamps to zero
    Given a workload exists for trainer "clamp.trainer" firstName "Clamp" lastName "Trainer" date "2026-04-01" duration 20
    When I delete workload for trainer "clamp.trainer" firstName "Clamp" lastName "Trainer" date "2026-04-01" duration 50
    Then the workload response status should be 204
    When I query monthly workload for trainer "clamp.trainer" year 2026 month 4
    Then the workload response status should be 200
    And the monthly duration should be 0

  Scenario: Get full trainer summary
    Given a workload exists for trainer "summ.trainer" firstName "Summ" lastName "Trainer" date "2026-05-10" duration 120
    When I query full workload summary for trainer "summ.trainer"
    Then the workload response status should be 200
    And the summary contains trainer username "summ.trainer"

  Scenario: Query monthly workload for unknown trainer returns 404
    When I query monthly workload for trainer "ghost.trainer" year 2026 month 1
    Then the workload response status should be 404

  Scenario: Query full summary for unknown trainer returns 404
    When I query full workload summary for trainer "nobody.here"
    Then the workload response status should be 404

  Scenario: Add workload requires JWT authentication
    When I add a workload without authentication for trainer "any.trainer"
    Then the workload response status should be 401

  Scenario: Add workload with invalid body returns 400
    When I add an invalid workload for trainer "bad.body" without required fields
    Then the workload response status should be 400