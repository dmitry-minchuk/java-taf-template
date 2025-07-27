package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightProjectDetailsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement modulesHeaderElement;
    private PlaywrightWebElement addModuleBtn;

    public PlaywrightProjectDetailsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightProjectDetailsComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        modulesHeaderElement = createScopedElement(".//h3/span[text()='Modules']", "modulesHeaderElement");
        addModuleBtn = createScopedElement(".//h3/span[text()='Modules']/following-sibling::a[@title='Add Module']", "addModuleBtn");
    }

    public void openAddModulePopup() {
        // Move to element and click - Playwright handles hover automatically
        addModuleBtn.click();
    }

    public boolean isModulesHeaderVisible() {
        return modulesHeaderElement.isVisible();
    }

    public boolean isAddModuleButtonVisible() {
        return addModuleBtn.isVisible();
    }
}