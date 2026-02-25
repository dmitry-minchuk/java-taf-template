package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class ProjectDetailsComponent extends BaseComponent {

    private WebElement modulesHeaderElement;
    private WebElement addModuleBtn;
    private WebElement editModuleHoverTemplate;
    private WebElement editModuleIconTemplate;
    private WebElement removeModuleHoverTemplate;
    private WebElement removeModuleIconTemplate;

    public ProjectDetailsComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ProjectDetailsComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        modulesHeaderElement = createScopedElement("xpath=.//h3/span[text()='Modules']", "modulesHeaderElement");
        addModuleBtn = createScopedElement("xpath=.//h3/span[text()='Modules']/following-sibling::a[@title='Add Module']", "addModuleBtn");
        editModuleHoverTemplate = createScopedElement("xpath=.//div[@class='list-item editable-inner']//a[contains(text(), '%s')]/../..", "editModuleHoverTemplate");
        editModuleIconTemplate = createScopedElement("xpath=.//div[@class='list list-modules']//a[contains(text(), '%s')]/../..//a[contains(@onclick, 'editModule')]/img", "editModuleIconTemplate");
        removeModuleHoverTemplate = createScopedElement("xpath=.//div[@class='list list-modules']//a[contains(text(), '%s')]", "removeModuleHoverTemplate");
        removeModuleIconTemplate = createScopedElement("xpath=.//div[@class='list list-modules']//a[contains(text(), '%s')]/../..//a[contains(@onclick, 'removeModule')]", "removeModuleIconTemplate");
    }

    public void openAddModulePopup() {
        modulesHeaderElement.hover();
        addModuleBtn.click();
    }

    public void openEditModuleDialog(String moduleName) {
        editModuleHoverTemplate.format(moduleName).hover();
        editModuleIconTemplate.format(moduleName).waitForVisible();
        editModuleIconTemplate.format(moduleName).click();
    }

    public void openRemoveModuleDialog(String moduleName) {
        removeModuleHoverTemplate.format(moduleName).hover();
        removeModuleIconTemplate.format(moduleName).waitForVisible();
        removeModuleIconTemplate.format(moduleName).click();
    }

    public boolean isModulesHeaderVisible() {
        return modulesHeaderElement.isVisible();
    }

    public boolean isAddModuleButtonVisible() {
        return addModuleBtn.isVisible();
    }
}
