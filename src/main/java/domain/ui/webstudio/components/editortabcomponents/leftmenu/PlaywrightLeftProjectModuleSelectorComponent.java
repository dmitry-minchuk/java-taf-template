package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;


// Handles clicking on project names and specific modules within projects
public class PlaywrightLeftProjectModuleSelectorComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement projectNameLink;
    private PlaywrightWebElement projectModuleLink;

    public PlaywrightLeftProjectModuleSelectorComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftProjectModuleSelectorComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // EXACT SAME locators as legacy LeftProjectModuleSelectorComponent
        projectNameLink = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']", "projectNameLink");
        projectModuleLink = createScopedElement("xpath=.//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']", "projectModuleLink");
    }

    public void selectProject(String projectName) {
        String selector = String.format("xpath=.//li/a[@class='projectName' and text()='%s']", projectName);
        PlaywrightWebElement projectLink = createScopedElement(selector, "projectLink");
        projectLink.click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        selectProject(projectName);
        WaitUtil.sleep(200);
        String selector = String.format("xpath=.//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']", projectName, projectModuleName);
        PlaywrightWebElement moduleLink = createScopedElement(selector, "moduleLink");
        moduleLink.waitForVisible();
        moduleLink.click();
    }
}