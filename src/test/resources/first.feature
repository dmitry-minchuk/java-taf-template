@ExamlpleTag
Feature: First testing feature

  @Smoke
  Scenario Outline: first scenario
    When first parameter is <n>
    Then second step with string parameter <what>

    Examples: some cukes
        | n  | what   |
        | 15 | Hello world!  |