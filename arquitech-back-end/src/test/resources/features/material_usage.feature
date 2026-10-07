Feature: Register material usage
  As a site supervisor
  I want to register the materials used on a construction site
  So that the available stock stays up to date

  Background:
    Given a supervisor has a project with a material that has a stock of 40

  Scenario: Usage within the available stock is registered
    When the supervisor registers a usage of 10 units
    Then the response status is 201
    And the material stock is 30

  Scenario: Usage above the available stock is rejected
    When the supervisor registers a usage of 50 units
    Then the response status is 400 with error code "INSUFFICIENT_STOCK"
    And the material stock is 40