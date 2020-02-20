package tests.cucumber;

import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        strict = true,
        plugin = {
                "pretty",
                "com.epam.reportportal.cucumber.ScenarioReporter"
        },
        features = "src/test/resources/features/api.feature",
        tags = {"@ExampleTag"},
        glue = "stepdefinitions"
)
public class CucumberApiTest extends BaseCucumberTest {
}
