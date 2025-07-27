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
        moduleNameField = createScopedElement(".//input[@id='moduleName']", "moduleNameField");
        modulePathField = createScopedElement(".//input[@id='modulePath']", "modulePathField");
        moduleSaveBtn = createScopedElement(".//input[@value='Save']", "moduleSaveBtn");
        moduleCancelBtn = createScopedElement(".//input[@value='Cancel']", "moduleCancelBtn");
        commonProperty = createScopedElement(".//table[@class='properties properties-form wide']//tr[.//span[contains(text(), '%s')]]", "commonProperty");
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