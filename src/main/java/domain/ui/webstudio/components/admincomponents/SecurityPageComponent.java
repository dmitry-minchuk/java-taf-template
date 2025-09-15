package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class SecurityPageComponent extends BaseComponent {

    private WebElement singleUserModeRadio;
    private WebElement multiUserModeRadio;
    private WebElement activeDirectoryModeRadio;
    private WebElement samlModeRadio;
    private WebElement oauth2ModeRadio;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public SecurityPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SecurityPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        singleUserModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='single']", "singleUserModeRadio");
        multiUserModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='multi']", "multiUserModeRadio");
        activeDirectoryModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='ad']", "activeDirectoryModeRadio");
        samlModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='saml']", "samlModeRadio");
        oauth2ModeRadio = createScopedElement("xpath=.//input[@type='radio' and @value='oauth2']", "oauth2ModeRadio");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");
    }

    public void selectSingleUserMode() {
        singleUserModeRadio.click();
    }

    public void selectMultiUserMode() {
        multiUserModeRadio.click();
    }

    public void selectActiveDirectoryMode() {
        activeDirectoryModeRadio.click();
    }

    public void selectSamlMode() {
        samlModeRadio.click();
    }

    public void selectOAuth2Mode() {
        oauth2ModeRadio.click();
    }

    public boolean isSingleUserModeSelected() {
        return singleUserModeRadio.isChecked();
    }

    public boolean isMultiUserModeSelected() {
        return multiUserModeRadio.isChecked();
    }

    public boolean isActiveDirectoryModeSelected() {
        return activeDirectoryModeRadio.isChecked();
    }

    public boolean isSamlModeSelected() {
        return samlModeRadio.isChecked();
    }

    public boolean isOAuth2ModeSelected() {
        return oauth2ModeRadio.isChecked();
    }

    public void saveSecuritySettings() {
        saveBtn.click();
    }

    public void cancelSecuritySettings() {
        cancelBtn.click();
    }
}