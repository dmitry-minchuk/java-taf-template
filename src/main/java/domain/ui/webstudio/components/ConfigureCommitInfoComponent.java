package domain.ui.webstudio.components;

import configuration.core.SmartWebElement;
import domain.ui.BasePageComponent;
import net.datafaker.Faker;
import org.openqa.selenium.support.FindBy;

public class ConfigureCommitInfoComponent extends BasePageComponent {

    @FindBy(xpath = ".//input[@id='commit-user-email']")
    private SmartWebElement emailField;

    @FindBy(xpath = ".//input[@id='commit-user-firstname']")
    private SmartWebElement firstNameField;

    @FindBy(xpath = ".//input[@id='commit-user-lastname']")
    private SmartWebElement lastNameField;

    @FindBy(xpath = ".//select[@name='commit-user-display-name-select-box']")
    private SmartWebElement displayNameDropdown;

    @FindBy(xpath = ".//input[@id='commit-user-display-name']")
    private SmartWebElement otherDisplayNameField;

    @FindBy(xpath = ".//input[@id='save-commit-info']")
    private SmartWebElement saveBtn;

    @FindBy(xpath = ".//input[@value='Cancel']")
    private SmartWebElement cancelBtn;

    public ConfigureCommitInfoComponent() {
    }

    public void fillCommitInfoWithRandomData() {
        Faker faker = new Faker();
        emailField.sendKeys(faker.internet().emailAddress());
        firstNameField.sendKeys(faker.name().firstName());
        lastNameField.sendKeys(faker.name().lastName());
        saveBtn.click();
    }

}
