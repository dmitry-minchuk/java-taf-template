package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightSecurityPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement singleUserModeRadio;
    private PlaywrightWebElement multiUserModeRadio;
    private PlaywrightWebElement activeDirectoryModeRadio;
    private PlaywrightWebElement samlModeRadio;
    private PlaywrightWebElement oauth2ModeRadio;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightSecurityPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightSecurityPageComponent(PlaywrightWebElement rootLocator) {
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
        return singleUserModeRadio.isSelected();
    }

    public boolean isMultiUserModeSelected() {
        return multiUserModeRadio.isSelected();
    }

    public boolean isActiveDirectoryModeSelected() {
        return activeDirectoryModeRadio.isSelected();
    }

    public boolean isSamlModeSelected() {
        return samlModeRadio.isSelected();
    }

    public boolean isOAuth2ModeSelected() {
        return oauth2ModeRadio.isSelected();
    }

    public void saveSecuritySettings() {
        saveBtn.click();
    }

    public void cancelSecuritySettings() {
        cancelBtn.click();
    }
}