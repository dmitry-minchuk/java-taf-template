package tests.cucumber;

import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        strict = true,
        plugin = {
                "pretty",
                "com.epam.reportportal.cucumber.StepReporter"
        },
        features = "src/test/resources/features/first.feature",
        tags = {"@ExampleTag"},
        glue = "stepdefinitions"
)

public class CucumberTest extends BaseCucumberTest {
}
