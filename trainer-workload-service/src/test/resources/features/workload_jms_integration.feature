Feature: Workload JMS message processing integration

  These scenarios test the JMS consumer path inside trainer-workload-service:
  a WorkloadEventRequest arrives on the queue, the consumer processes it,
  and the result is verifiable via the REST API.

  Background:
    Given a valid JWT token for trainer-workload-service tests

  Scenario: JMS ADD event persists workload and is queryable via REST
    When a JMS ADD event arrives for trainer "jms.add" firstName "Jms" lastName "Add" date "2026-07-10" duration 90
    Then the JMS processing should complete without errors
    When I query monthly workload for trainer "jms.add" year 2026 month 7
    Then the workload response status should be 200
    And the monthly duration should be 90

  Scenario: JMS ADD followed by DELETE produces correct net duration
    When a JMS ADD event arrives for trainer "jms.del" firstName "Jms" lastName "Del" date "2026-08-05" duration 120
    And a JMS DELETE event arrives for trainer "jms.del" firstName "Jms" lastName "Del" date "2026-08-05" duration 40
    Then the JMS processing should complete without errors
    When I query monthly workload for trainer "jms.del" year 2026 month 8
    Then the workload response status should be 200
    And the monthly duration should be 80

  Scenario: JMS ADD events for different months accumulate independently
    When a JMS ADD event arrives for trainer "jms.months" firstName "Jms" lastName "Months" date "2026-01-15" duration 60
    And a JMS ADD event arrives for trainer "jms.months" firstName "Jms" lastName "Months" date "2026-02-20" duration 45
    When I query monthly workload for trainer "jms.months" year 2026 month 1
    Then the workload response status should be 200
    And the monthly duration should be 60
    When I query monthly workload for trainer "jms.months" year 2026 month 2
    Then the workload response status should be 200
    And the monthly duration should be 45

  Scenario: JMS DELETE below zero clamps to zero
    When a JMS ADD event arrives for trainer "jms.clamp" firstName "Jms" lastName "Clamp" date "2026-09-01" duration 30
    And a JMS DELETE event arrives for trainer "jms.clamp" firstName "Jms" lastName "Clamp" date "2026-09-01" duration 100
    When I query monthly workload for trainer "jms.clamp" year 2026 month 9
    Then the workload response status should be 200
    And the monthly duration should be 0

  Scenario: Trainer with no events has no workload record (JMS equivalent: no messages sent)
    When I query monthly workload for trainer "never.trained" year 2026 month 10
    Then the workload response status should be 404