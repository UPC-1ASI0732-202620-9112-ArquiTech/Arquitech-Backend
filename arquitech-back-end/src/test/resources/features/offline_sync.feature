Feature: Synchronize offline material drafts once (EC08)
  As a site supervisor working without connection
  I want my saved drafts to be applied only once
  So that retries never duplicate inventory movements

  Background:
    Given a supervisor has a project with a material that has a stock of 40

  Scenario: Replaying the same draft does not duplicate the usage
    When the supervisor syncs the offline usage "draft-tb2-0001" of 5 units
    Then the response status is 201
    When the supervisor syncs the offline usage "draft-tb2-0001" of 5 units
    Then the response status is 200
    And the material stock is 35
    And the project history has 1 usage movements

  Scenario: Reusing a draft id with different data is rejected
    When the supervisor syncs the offline usage "draft-tb2-0002" of 5 units
    And the supervisor syncs the offline usage "draft-tb2-0002" of 7 units
    Then the response status is 409 with error code "OFFLINE_REQUEST_CONFLICT"
    And the material stock is 35
