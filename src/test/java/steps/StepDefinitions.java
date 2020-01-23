package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class StepDefinitions {

    @When("first parameter is {int}")
    public void firstStep(int arg) {
        System.out.println("This is a first step with parameter: " + arg);
    }

    @Then("^second step with string parameter (.+)$")
    public void secondStep(String arg) {
        System.out.println("This is a second step with String parameter: " + arg);
    }
}
