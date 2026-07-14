package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import net.datafaker.Faker;

// React "Configure Git Commit Info" modal (build 032c60a664ce+). Fields are #firstName / #lastName /
// #email / #displayName; footer has Cancel / Save (primary, no data-testid).
public class ConfigureCommitInfoComponent extends BaseComponent {

    private WebElement emailField;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement displayNameField;
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
        emailField = createScopedElement("xpath=.//input[@id='email']", "emailField");
        firstNameField = createScopedElement("xpath=.//input[@id='firstName']", "firstNameField");
        lastNameField = createScopedElement("xpath=.//input[@id='lastName']", "lastNameField");
        displayNameField = createScopedElement("xpath=.//input[@id='displayName']", "displayNameField");
        saveBtn = createScopedElement("xpath=.//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Save']]", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Cancel']]", "cancelBtn");
    }

    public void fillCommitInfoWithRandomData() {
        Faker faker = new Faker();
        firstNameField.fill(faker.name().firstName());
        lastNameField.fill(faker.name().lastName());
        // Email and Display Name are required in the React modal — fill unconditionally (fill() waits).
        emailField.fill(faker.internet().emailAddress());
        displayNameField.fill(faker.name().fullName());
        saveBtn.click();
    }
}
