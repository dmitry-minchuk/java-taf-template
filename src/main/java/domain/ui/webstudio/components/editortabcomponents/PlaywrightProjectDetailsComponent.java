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
        modulesHeaderElement = new PlaywrightWebElement(page, ".//h3/span[text()='Modules']", "Modules Header Element");
        addModuleBtn = new PlaywrightWebElement(page, ".//h3/span[text()='Modules']/following-sibling::a[@title='Add Module']", "Add Module Button");
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