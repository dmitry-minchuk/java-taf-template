package tests.cucumber;

import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        strict = true,
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-pretty",
                "json:target/cucumber-reports/CucumberFirstFeatureReport.json"
        },
        features = "src/test/resources/features/first.feature",
        tags = {"@ExamlpleTag"},
        glue = "stepdefinitions"
)
public class CucumberTest extends BaseCucumberTest {
}