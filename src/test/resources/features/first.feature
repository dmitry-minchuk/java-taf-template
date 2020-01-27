@ExamlpleTag
Feature: Testing google page

  @Smoke
  Scenario: Searhing something in google...
    When User navigates to "google.com"
    And User enters "iphone" in search field "input[name=q]"
    And User presses Escape Btn
    And User clicks Search Button "div:not([jsname]) > center > input:nth-child(1)"