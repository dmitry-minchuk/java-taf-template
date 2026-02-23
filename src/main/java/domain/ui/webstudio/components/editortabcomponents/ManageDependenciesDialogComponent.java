package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class ManageDependenciesDialogComponent extends BaseComponent {

    private WebElement saveBtn;
    private WebElement cancelBtn;
    private WebElement closeBtn;
    private WebElement projectCheckboxTemplate;
    private WebElement includeAllModulesCheckboxTemplate;

    public ManageDependenciesDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ManageDependenciesDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        saveBtn = createScopedElement("xpath=.//form[@id='manageDependenciesForm']//input[@id='editDependenciesBtn']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='manageDependenciesForm']//input[@value='Cancel']", "cancelBtn");
        closeBtn = createScopedElement("xpath=.//div[@id='manageDependenciesPopup_header_controls']//img[@alt='Close']", "closeBtn");
        projectCheckboxTemplate = createScopedElement("xpath=.//label[contains(text(), '%s')]//input", "projectCheckboxTemplate");
        includeAllModulesCheckboxTemplate = createScopedElement("xpath=.//label[contains(text(), '%s')]/parent::*/parent::*//input[@title='Include all modules']", "includeAllModulesCheckboxTemplate");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> cancelBtn.isVisible(), 5000, 100, "Waiting for Manage Dependencies dialog to appear");
    }

    public ManageDependenciesDialogComponent selectProject(String projectName) {
        projectCheckboxTemplate.format(projectName).click();
        return this;
    }

    public ManageDependenciesDialogComponent setIncludeAllModules(String projectName, boolean includeAll) {
        WebElement checkbox = includeAllModulesCheckboxTemplate.format(projectName);
        boolean isChecked = checkbox.isChecked();
        if (includeAll && !isChecked) {
            checkbox.click();
        } else if (!includeAll && isChecked) {
            checkbox.click();
        }
        return this;
    }

    public void addDependency(String projectName, boolean includeAllModules) {
        selectProject(projectName);
        setIncludeAllModules(projectName, includeAllModules);
        saveBtn.click();
    }

    public void clickCancel() {
        cancelBtn.click();
    }

    public void clickClose() {
        closeBtn.click();
    }
}
