package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import net.datafaker.Faker;

public class ConfigureCommitInfoComponent extends BaseComponent {

    private WebElement emailField;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement displayNameDropdown;
    private WebElement otherDisplayNameField;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public ConfigureCommitInfoComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ConfigureCommitInfoComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        emailField = createScopedElement("xpath=.//input[@id='commit-user-email']", "emailField");
        firstNameField = createScopedElement("xpath=.//input[@id='commit-user-firstname']", "firstNameField");
        lastNameField = createScopedElement("xpath=.//input[@id='commit-user-lastname']", "lastNameField");
        displayNameDropdown = createScopedElement("xpath=.//select[@name='commit-user-display-name-select-box']", "displayNameDropdown");
        otherDisplayNameField = createScopedElement("xpath=.//input[@id='commit-user-display-name']", "otherDisplayNameField");
        saveBtn = createScopedElement("xpath=.//input[@id='save-commit-info']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void fillCommitInfoWithRandomData() {
        Faker faker = new Faker();
        emailField.sendKeys(faker.internet().emailAddress());
        firstNameField.sendKeys(faker.name().firstName());
        lastNameField.sendKeys(faker.name().lastName());
        saveBtn.click();
    }
}