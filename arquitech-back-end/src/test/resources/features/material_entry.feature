Feature: Register material entries from the guided form (EC01)
  As a site supervisor
  I want to register incoming materials without retyping the supplier
  So that the stock is updated with one confirmation

  Background:
    Given a supervisor has a project with a material that has a stock of 40

  Scenario: An entry without supplier or note is registered
    When the supervisor registers an entry of 10 units without supplier
    Then the response status is 201
    And the material stock is 50

  Scenario: An entry with an invalid quantity is rejected
    When the supervisor registers an entry of 0 units without supplier
    Then the response status is 400 with error code "VALIDATION_ERROR"
    And the material stock is 40
