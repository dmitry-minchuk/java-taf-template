package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import lombok.Getter;

@Getter
public class AddModuleComponent extends BaseComponent {

    private WebElement moduleNameField;
    private WebElement modulePathField;
    private WebElement moduleSaveBtn;
    private WebElement moduleCancelBtn;
    private WebElement commonProperty;

    public AddModuleComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public AddModuleComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        moduleNameField = createScopedElement("xpath=.//input[@id='moduleName']", "moduleNameField");
        modulePathField = createScopedElement("xpath=.//input[@id='modulePath']", "modulePathField");
        moduleSaveBtn = createScopedElement("xpath=.//input[@value='Save']", "moduleSaveBtn");
        moduleCancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "moduleCancelBtn");
        commonProperty = createScopedElement("xpath=.//span[@class='error'][contains(text(), '%s')]", "commonProperty");
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
        moduleSaveBtn.press("Tab");
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

    public boolean isCancelButtonVisible() {
        return moduleCancelBtn.isVisible();
    }
    
    public void fillForm(String moduleName, String modulePath) {
        moduleNameField.fill(moduleName);
        modulePathField.fill(modulePath);
        moduleSaveBtn.click();
    }
    
    public boolean isSpecificPropertyShown(String text) {
        return commonProperty.format(text).isVisible(DEFAULT_TIMEOUT_MS);
    }
}