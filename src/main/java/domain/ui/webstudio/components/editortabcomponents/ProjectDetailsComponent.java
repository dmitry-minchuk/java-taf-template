package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class ProjectDetailsComponent extends CoreComponent {

    private WebElement modulesHeaderElement;
    private WebElement addModuleBtn;

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
    }

    public void openAddModulePopup() {
        modulesHeaderElement.hover();
        addModuleBtn.click();
    }

    public boolean isModulesHeaderVisible() {
        return modulesHeaderElement.isVisible();
    }

    public boolean isAddModuleButtonVisible() {
        return addModuleBtn.isVisible();
    }
}