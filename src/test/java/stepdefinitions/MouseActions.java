package stepdefinitions;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.How;
import utils.ActionsUtil;

import static com.codeborne.selenide.Condition.text;

public class MouseActions {
    private final String hoverByCss = "^User moves mouse over .* \"([^\"]*)\"$";
    private final String hoverByCssWithText = "^User moves mouse over .* \"([^\"]*)\" with text \"([^\"]*)\"$";

    @When(value = hoverByCss)
    public void hoverByCss(String locator) {
        ActionsUtil.findElement(How.CSS, locator).hover();
        Selenide.sleep(1000);
    }

    @When(value = hoverByCssWithText)
    public void hoverByCssWithText(String locator, String text) {
        ActionsUtil.findElements(How.CSS, locator).find(text(text)).hover();
        Selenide.sleep(1000);
    }
}
