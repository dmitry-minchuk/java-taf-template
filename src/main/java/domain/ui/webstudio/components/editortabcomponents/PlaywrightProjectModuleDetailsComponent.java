package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightProjectModuleDetailsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement moduleDetailsPanel;
    private PlaywrightWebElement moduleNameHeader;
    private PlaywrightWebElement modulePathHeader;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightProjectModuleDetailsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightProjectModuleDetailsComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        moduleDetailsPanel = new PlaywrightWebElement(page, ".//div[contains(@class,'module-details')]", "Module Details Panel");
        moduleNameHeader = new PlaywrightWebElement(page, ".//h3[contains(@class,'module-name')] | .//span[contains(@class,'module-name')]", "Module Name Header");
        modulePathHeader = new PlaywrightWebElement(page, ".//span[contains(@class,'module-path')] | .//div[contains(@class,'module-path')]", "Module Path Header");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
    }

    public boolean isModuleDetailsPanelVisible() {
        return moduleDetailsPanel.isVisible();
    }

    public String getModuleName() {
        return moduleNameHeader.getText();
    }

    public String getModulePath() {
        return modulePathHeader.getText();
    }

    public void saveModuleDetails() {
        saveBtn.click();
    }

    public void cancelModuleDetails() {
        cancelBtn.click();
    }

    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
    }

    public boolean isCancelButtonVisible() {
        return cancelBtn.isVisible();
    }
}