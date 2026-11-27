Feature: Quick daily attendance (EC02)
  As a site supervisor
  I want to confirm the attendance of the day for every active worker
  So that the record is complete without editing worker by worker

  Background:
    Given a supervisor has a project with a material that has a stock of 40
    And the project has 3 active workers

  Scenario: Marking every worker as present registers the whole day
    When the supervisor records every worker as "PRESENT" on "2026-11-20"
    Then 3 attendance records exist for "2026-11-20"

  Scenario: A second record for the same worker and date is rejected
    When the supervisor records every worker as "PRESENT" on "2026-11-21"
    And the supervisor records the first worker as "ABSENT" on "2026-11-21"
    Then the response status is 409 with error code "DUPLICATE_ATTENDANCE"
    And 3 attendance records exist for "2026-11-21"
