package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

public class EditorMainContentProblemsPanelComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@class='problem-error']")
    private SmartWebElement errorMessage;

    @FindBy(xpath = ".//div[@class='problem-warning']")
    private SmartWebElement warningMessage;

    @FindBy(xpath = ".//div[@id='problemsPanel']")
    private SmartWebElement allErrorsBlock;

    public EditorMainContentProblemsPanelComponent() {
    }

    public boolean isErrorMessagePresent() {
        return errorMessage.isDisplayed(2);
    }

    public boolean isWarningMessagePresent() {
        return warningMessage.isDisplayed(2);
    }

    public String getErrorMessageText() {
        if (errorMessage.isDisplayed(2)) {
            return errorMessage.getText();
        }
        return "";
    }

    public String getWarningMessageText() {
        if (warningMessage.isDisplayed(2)) {
            return warningMessage.getText();
        }
        return "";
    }

    public boolean isAllErrorsBlockPresent() {
        return allErrorsBlock.isDisplayed(2);
    }
}