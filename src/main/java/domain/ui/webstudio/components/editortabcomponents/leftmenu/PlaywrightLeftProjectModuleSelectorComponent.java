package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

/**
 * Playwright version of LeftProjectModuleSelectorComponent for project and module selection
 * Handles clicking on project names and specific modules within projects
 */
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
        // Project name link: ".//li/a[@class='projectName' and text()='%s']"
        projectNameLink = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']", "projectNameLink");
        
        // Project module link: ".//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']"
        projectModuleLink = createScopedElement("xpath=.//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']", "projectModuleLink");
    }

    public void selectProject(String projectName) {
        projectNameLink.format(projectName).click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        projectModuleLink.format(projectName, projectModuleName).click();
    }
}