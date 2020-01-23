import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        strict = true,
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-pretty",
                "json:target/cucumber-reports/CucumberFirstFeatureReport.json"
        },
        features = "src/test/resources",
        tags = {"@ExamlpleTag"},
        glue = "steps"
)
public class CukeTest extends AbstractTestNGCucumberTests {
}
