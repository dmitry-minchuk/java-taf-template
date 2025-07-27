package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import net.datafaker.Faker;

/**
 * Playwright version of ConfigureCommitInfoComponent - handles commit configuration modal
 * This modal appears after project creation and needs to be filled or dismissed
 * Matches original ConfigureCommitInfoComponent functionality exactly
 */
public class PlaywrightConfigureCommitInfoComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement emailField;
    private PlaywrightWebElement firstNameField;
    private PlaywrightWebElement lastNameField;
    private PlaywrightWebElement displayNameDropdown;
    private PlaywrightWebElement otherDisplayNameField;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightConfigureCommitInfoComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightConfigureCommitInfoComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Email field: ".//input[@id='commit-user-email']"
        emailField = createScopedElement("xpath=.//input[@id='commit-user-email']", "emailField");
        
        // First name field: ".//input[@id='commit-user-firstname']"
        firstNameField = createScopedElement("xpath=.//input[@id='commit-user-firstname']", "firstNameField");
        
        // Last name field: ".//input[@id='commit-user-lastname']"
        lastNameField = createScopedElement("xpath=.//input[@id='commit-user-lastname']", "lastNameField");
        
        // Display name dropdown: ".//select[@name='commit-user-display-name-select-box']"
        displayNameDropdown = createScopedElement("xpath=.//select[@name='commit-user-display-name-select-box']", "displayNameDropdown");
        
        // Other display name field: ".//input[@id='commit-user-display-name']"
        otherDisplayNameField = createScopedElement("xpath=.//input[@id='commit-user-display-name']", "otherDisplayNameField");
        
        // Save button: ".//input[@id='save-commit-info']"
        saveBtn = createScopedElement("xpath=.//input[@id='save-commit-info']", "saveBtn");
        
        // Cancel button: ".//input[@value='Cancel']"
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    /**
     * Fill commit info with random data - matches original functionality exactly
     */
    public void fillCommitInfoWithRandomData() {
        Faker faker = new Faker();
        emailField.sendKeys(faker.internet().emailAddress());
        firstNameField.sendKeys(faker.name().firstName());
        lastNameField.sendKeys(faker.name().lastName());
        saveBtn.click();
    }
    
    /**
     * Legacy-compatible isPresent method
     */
    public boolean isPresent() {
        if (rootLocator != null) {
            try {
                return rootLocator.isDisplayed();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}