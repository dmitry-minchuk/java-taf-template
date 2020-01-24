package stepdefinitions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.How;
import utils.JsUtil;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ClickSteps {

    private final String clickByCss = "^User clicks .* (.+)$";
    private final String clickByCssWithText = "^User clicks .* (.+) with text (.+)$";
    private final String clickByCssByExecutingScript = "^User clicks .* (.+) by executing script$";
    private final String clickByCssWithTextByExecutingScript = "^User clicks .* (.+) with text (.+) by executing script$";
    private final String clickByCssWithExactText = "^User clicks .* (.+) with text equal to (.+)$";
    private final String clickBrowserBackButton = "User clicks browser back button";

    @When(value = clickByCss)
    @And(value = clickByCss)
    public void clickByCss(String locator) {
        findElement(How.CSS, locator).click();
    }

    @When(value = clickByCssWithText)
    @And(value = clickByCssWithText)
    public void clickByCssWithText(String locator, String text) {
        findElements(How.CSS, locator).findBy(text(text)).click();
    }

    @When(value = clickByCssByExecutingScript)
    @And(value = clickByCssByExecutingScript)
    public void clickByCssByExecutingScript(String locator) {
        SelenideElement element = findElement(How.CSS, locator);
        JsUtil.clickElement(WebDriverRunner.getWebDriver(), element.getWrappedElement());
    }

    @When(value = clickByCssWithTextByExecutingScript)
    @And(value = clickByCssWithTextByExecutingScript)
    public void clickByCssWithTextByExecutingScript(String locator, String text) {
        SelenideElement element = findElements(How.CSS, locator).findBy(text(text));
        JsUtil.clickElement(WebDriverRunner.getWebDriver(), element.getWrappedElement());
    }

    @When(value = clickByCssWithExactText)
    @And(value = clickByCssWithExactText)
    public void clickByCssWithExactText(String locator, String text) {
        findElements(How.CSS, locator).findBy(exactText(text)).click();
    }

    @When(value = clickBrowserBackButton)
    @And(value = clickBrowserBackButton)
    public void clickBrowserBackButton() {
        Selenide.back();
    }

    private SelenideElement findElement(How how, String locator) {
        return $(how.buildBy(locator));
    }

    private ElementsCollection findElements(How how, String locator) {
        return $$(how.buildBy(locator));
    }
}
