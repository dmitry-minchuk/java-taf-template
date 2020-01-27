package stepdefinitions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.How;
import utils.ActionsUtil;
import utils.JsUtil;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;

public class ClickSteps {

    private final String clickByCss = "^User clicks .* \"([^\"]*)\"$";
    private final String clickByCssWithText = "^User clicks .* \"([^\"]*)\" with text \"([^\"]*)\"$";
    private final String clickByCssByExecutingScript = "^User clicks .* \"([^\"]*)\" by executing script$";
    private final String clickByCssWithTextByExecutingScript = "^User clicks .* \"([^\"]*)\" with text \"([^\"]*)\" by executing script$";
    private final String clickByCssWithExactText = "^User clicks .* \"([^\"]*)\" with text equal to \"([^\"]*)\"$";
    private final String clickBrowserBackButton = "User clicks browser back button";
    private final String clickByCssOnChildInParent = "^User clicks .* \"([^\"]*)\" on .* \"([^\"]*)\"$";
    private final String clickByCssOnChildWithTextInParentWithText = "^User clicks .* \"([^\"]*)\" with text \"([^\"]*)\" on .* \"([^\"]*)\" with text \"([^\"]*)\"$";
    private final String clickByCssOnChildInParentWithText = "^User clicks .* \"([^\"]*)\" on .* \"([^\"]*)\" with text \"([^\"]*)\"$";
    private final String doubleClickByCss = "^User double clicks .* \"([^\"]*)\"$";

    @When(value = clickByCss)
    public void clickByCss(String locator) {
        ActionsUtil.findElement(How.CSS, locator).click();
    }

    @When(value = clickByCss)
    public void doubleClickByCss(String locator) {
        ActionsUtil.findElement(How.CSS, locator).doubleClick();
    }

    @When(value = clickByCssWithText)
    public void clickByCssWithText(String locator, String text) {
        ActionsUtil.findElements(How.CSS, locator).findBy(text(text)).click();
    }

    @When(value = clickByCssByExecutingScript)
    public void clickByCssByExecutingScript(String locator) {
        SelenideElement element = ActionsUtil.findElement(How.CSS, locator);
        JsUtil.clickElement(WebDriverRunner.getWebDriver(), element.getWrappedElement());
    }

    @When(value = clickByCssWithTextByExecutingScript)
    public void clickByCssWithTextByExecutingScript(String locator, String text) {
        SelenideElement element = ActionsUtil.findElements(How.CSS, locator).findBy(text(text));
        JsUtil.clickElement(WebDriverRunner.getWebDriver(), element.getWrappedElement());
    }

    @When(value = clickByCssWithExactText)
    public void clickByCssWithExactText(String locator, String text) {
        ActionsUtil.findElements(How.CSS, locator).findBy(exactText(text)).click();
    }

    @When(value = clickBrowserBackButton)
    public void clickBrowserBackButton() {
        Selenide.back();
    }

    @When(value = clickByCssOnChildInParent)
    public void clickByCssOnChildInParent(String childLocator, String parentLocator) {
        ActionsUtil.findElement(How.CSS, parentLocator).find(How.CSS.buildBy(childLocator)).click();
    }

    @When(value = clickByCssOnChildWithTextInParentWithText)
    public void clickByCssOnChildWithTextInParentWithText(String childLocator, String childText, String parentLocator, String parentText) {
        ActionsUtil.findElements(How.CSS, parentLocator).findBy(text(parentText)).findAll(How.CSS.buildBy(childLocator)).findBy(text(childText)).click();
    }

    @When(value = clickByCssOnChildInParentWithText)
    public void clickByCssOnChildInParentWithText(String childLocator, String parentLocator, String parentText) {
        ActionsUtil.findElements(How.CSS, parentLocator).findBy(text(parentText)).find(How.CSS.buildBy(childLocator)).click();
    }
}
