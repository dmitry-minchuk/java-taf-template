package tests.cucumber;

import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        strict = true,
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-pretty",
                "json:target/cucumber-reports/CucumberFirstFeatureReport.json"
        },
        features = "src/test/resources/features/api.feature",
        tags = {"@ExampleTag"},
        glue = "stepdefinitions"
)
public class CucumberApiTest extends BaseCucumberTest {
}
