package stepdefinitions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.How;
import org.testng.Assert;
import utils.ActionsUtil;
import utils.JsUtil;

import static com.codeborne.selenide.Condition.*;
import static utils.ActionsUtil.findElement;

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
    private final String clickNthElementInCssCollection = "^User clicks \"([^\"]*)\" item in \"([^\"]*)\" collection$";
    private final String clickNthElementInCssCollectionWithText = "^User clicks \"([^\"]*)\" item in \"([^\"]*)\" collection with text \"([^\"]*)\"$";
    private final String clickByCssUntilElementVisible = "^User clicks on .* \"([^\"]*)\" until it visible$";
    private final String clickByCssUntilAnotherElementBecomeVisible = "^User clicks on .* \"([^\"]*)\" until .* \"([^\"]*)\" with text \"([^\"]*)\" will become visible$";

    @When(value = clickByCss)
    public void clickByCss(String locator) {
        findElement(How.CSS, locator).click();
    }

    @When(value = doubleClickByCss)
    public void doubleClickByCss(String locator) {
        findElement(How.CSS, locator).doubleClick();
    }

    @When(value = clickNthElementInCssCollection)
    public void clickNthElementInCssCollection(String i, String locator) {
        ElementsCollection collection = ActionsUtil.findElements(How.CSS, locator);
        Assert.assertEquals(collection.size(), i + 1, "Desired element index is out of range!");
        collection.get(Integer.parseInt(i)).click();
    }

    @When(value = clickNthElementInCssCollectionWithText)
    public void clickNthElementInCssCollectionWithText(String i, String locator, String text) {
        ElementsCollection collection = ActionsUtil.findElements(How.CSS, locator).filter(text(text));
        Assert.assertEquals(collection.size(), i + 1, "Desired element index is out of range!");
        collection.get(Integer.parseInt(i)).click();
    }

    @When(value = clickByCssUntilElementVisible)
    public void clickByCssUntilElementVisible(String locator) {
        for (int i = 0; i < 15 && ActionsUtil.findElement(How.CSS, locator).is(visible); i++) {
            ActionsUtil.findElement(How.CSS, locator).click();
            Selenide.sleep(1000);
        }
    }

    @When(value = clickByCssUntilAnotherElementBecomeVisible)
    public void clickByCssUntilAnotherElementBecomeVisible(String locator1, String locator2, String text) {
        for (int i = 0; i < 15 && !ActionsUtil.findElements(How.CSS, locator2).find(text(text)).is(visible); i++) {
            ActionsUtil.findElement(How.CSS, locator1).click();
            Selenide.sleep(1000);
        }
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
        ActionsUtil.findElements(How.CSS, parentLocator).find(text(parentText)).findAll(How.CSS.buildBy(childLocator)).find(text(childText)).click();
    }

    @When(value = clickByCssOnChildInParentWithText)
    public void clickByCssOnChildInParentWithText(String childLocator, String parentLocator, String parentText) {
        ActionsUtil.findElements(How.CSS, parentLocator).find(text(parentText)).find(How.CSS.buildBy(childLocator)).click();
    }
}
