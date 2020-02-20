package tests.cucumber;

import com.epam.reportportal.testng.ReportPortalTestNGListener;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;

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

@Listeners(ReportPortalTestNGListener.class)
public class CucumberTest extends BaseCucumberTest {
}
