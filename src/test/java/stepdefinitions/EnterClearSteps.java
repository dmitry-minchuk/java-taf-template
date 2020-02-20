package stepdefinitions;

import io.cucumber.java.en.When;
import org.openqa.selenium.support.How;
import utils.ActionsUtil;

public class EnterClearSteps {

    private final String enterText = "^User enters \"([^\"]*)\" in .* \"([^\"]*)\"$";

    @When(value = enterText)
    public void enterText(String text, String locator) {
        ActionsUtil.findElement(How.CSS, locator).setValue(text);
    }
}
