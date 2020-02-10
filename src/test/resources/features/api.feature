@ExampleTag
Feature: Api testing feature

  @Smoke
  Scenario: Api scenario
    When User requests "/get" endpoint
    Then Status code is 200