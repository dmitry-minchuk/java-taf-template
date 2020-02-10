@ExampleTag
Feature: Second testing feature

  @Regression
  Scenario Outline: second scenario
     When first parameter is <n>
     Then second step with string parameter <what>

     Examples: some cukes
        | n  | what   |
        | 25 | Opps, it's 25  |
        | 30 | Opps, it's 30  |