package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class ProjectModuleDetailsComponent extends CoreComponent {

    private WebElement moduleDetailsPanel;
    private WebElement moduleNameHeader;
    private WebElement modulePathHeader;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public ProjectModuleDetailsComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ProjectModuleDetailsComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        moduleDetailsPanel = createScopedElement(".//div[contains(@class,'module-details')]", "moduleDetailsPanel");
        moduleNameHeader = createScopedElement(".//h3[contains(@class,'module-name')] | .//span[contains(@class,'module-name')]", "moduleNameHeader");
        modulePathHeader = createScopedElement(".//span[contains(@class,'module-path')] | .//div[contains(@class,'module-path')]", "modulePathHeader");
        saveBtn = createScopedElement(".//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement(".//button[./span[text()='Cancel']]", "cancelBtn");
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

    public boolean isCancelButtonVisible() {
        return cancelBtn.isVisible();
    }
}