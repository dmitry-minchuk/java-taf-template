package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

public class RepositoryContentButtonsPanelComponent extends BasePageComponent {

    @FindBy(xpath = ".//input[@value='Close']")
    private SmartWebElement closeButton;

    @FindBy(xpath = ".//input[@value='Save']")
    private SmartWebElement saveButton;

    @FindBy(xpath = ".//input[@value='Copy']")
    private SmartWebElement copyButton;

    @FindBy(xpath = ".//input[@value='Delete']")
    private SmartWebElement deleteButton;

    @FindBy(xpath = ".//input[@value='Deploy']")
    private SmartWebElement deployButton;

    public void saveDeploy() {
        saveButton.click(5);
    }

    public void clickDeploy() {
        deployButton.click();
    }

    public boolean isDeployButtonEnabled() {
        return deployButton.isDisplayed() && deployButton.isEnabled();
    }
} 