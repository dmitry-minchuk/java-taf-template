package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class EditModuleDialogComponent extends BaseComponent {

    private WebElement compileThisModuleOnlyCheckbox;
    private WebElement moduleNameField;
    private WebElement includedMethodsField;
    private WebElement excludedMethodsField;
    private WebElement saveBtn;
    private WebElement closeBtn;

    public EditModuleDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public EditModuleDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        compileThisModuleOnlyCheckbox = createScopedElement("xpath=.//input[@id='compileThisModuleOnly']", "compileThisModuleOnlyCheckbox");
        moduleNameField = createScopedElement("xpath=.//input[@id='moduleName']", "moduleNameField");
        includedMethodsField = createScopedElement("xpath=.//textarea[@id='moduleIncludes']", "includedMethodsField");
        excludedMethodsField = createScopedElement("xpath=.//textarea[@id='moduleExcludes']", "excludedMethodsField");
        saveBtn = createScopedElement("xpath=.//input[@value='Save']", "saveBtn");
        closeBtn = createScopedElement("xpath=.//input[@value='Cancel'] | .//a[@class='close']", "closeBtn");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> moduleNameField.isVisible(), 5000, 250, "Waiting for Edit Module dialog to appear");
    }

    public boolean isCompileThisModuleOnlyVisible() {
        return compileThisModuleOnlyCheckbox.isVisible(2000);
    }

    public boolean isCompileThisModuleOnlyAbsent() {
        return !compileThisModuleOnlyCheckbox.isVisible(1000);
    }

    public boolean isCompileThisModuleOnlyChecked() {
        return compileThisModuleOnlyCheckbox.isChecked();
    }

    public boolean isCompileThisModuleOnlyDisabled() {
        String classAttr = compileThisModuleOnlyCheckbox.getAttribute("class");
        return classAttr != null && classAttr.contains("disabled");
    }

    public String getModuleName() {
        return moduleNameField.getAttribute("value");
    }

    public void setModuleName(String name) {
        moduleNameField.clear();
        moduleNameField.fill(name);
    }

    /** One pattern per line, as the dialog's own hint says. */
    public void setIncludedMethods(String patterns) {
        includedMethodsField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(patterns);
    }

    public void setExcludedMethods(String patterns) {
        excludedMethodsField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(patterns);
    }

    public void clickSave() {
        saveBtn.click();
        WaitUtil.sleep(500, "Waiting after saving module changes");
    }

    public void clickClose() {
        closeBtn.click();
        WaitUtil.sleep(250, "Waiting for Edit Module dialog to close");
    }
}
