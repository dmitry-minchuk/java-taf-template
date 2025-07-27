package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

@Getter
public class PlaywrightAddModuleComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement moduleNameField;
    private PlaywrightWebElement modulePathField;
    private PlaywrightWebElement moduleSaveBtn;
    private PlaywrightWebElement moduleCancelBtn;
    private PlaywrightWebElement commonProperty;

    public PlaywrightAddModuleComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightAddModuleComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        moduleNameField = new PlaywrightWebElement(page, ".//input[@id='moduleName']", "Module Name Field");
        modulePathField = new PlaywrightWebElement(page, ".//input[@id='modulePath']", "Module Path Field");
        moduleSaveBtn = new PlaywrightWebElement(page, ".//input[@value='Save']", "Module Save Button");
        moduleCancelBtn = new PlaywrightWebElement(page, ".//input[@value='Cancel']", "Module Cancel Button");
        commonProperty = new PlaywrightWebElement(page, ".//table[@class='properties properties-form wide']//tr[.//span[contains(text(), '%s')]]", "Common Property");
    }

    public void setModuleName(String moduleName) {
        moduleNameField.fill(moduleName);
    }

    public String getModuleName() {
        return moduleNameField.getAttribute("value");
    }

    public void setModulePath(String modulePath) {
        modulePathField.fill(modulePath);
    }

    public String getModulePath() {
        return modulePathField.getAttribute("value");
    }

    public void saveModule() {
        moduleSaveBtn.click();
    }

    public void cancelModule() {
        moduleCancelBtn.click();
    }

    public void addModule(String moduleName, String modulePath) {
        setModuleName(moduleName);
        setModulePath(modulePath);
        saveModule();
    }

    public boolean isSaveButtonEnabled() {
        return moduleSaveBtn.isEnabled();
    }

    public boolean isCancelButtonVisible() {
        return moduleCancelBtn.isVisible();
    }
}