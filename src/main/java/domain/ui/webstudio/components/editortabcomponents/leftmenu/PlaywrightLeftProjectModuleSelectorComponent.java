package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;


// Handles clicking on project names and specific modules within projects
public class PlaywrightLeftProjectModuleSelectorComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement projectNameTemplate;
    private PlaywrightWebElement projectModuleTemplate;

    public PlaywrightLeftProjectModuleSelectorComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftProjectModuleSelectorComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        projectNameTemplate = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']", "projectNameLink");
        projectModuleTemplate = createScopedElement("xpath=.//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']", "projectModuleLink");
    }

    public void selectProject(String projectName) {
        projectNameTemplate.format(projectName).click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        selectProject(projectName);
        WaitUtil.sleep(200);
        PlaywrightWebElement moduleLink = projectModuleTemplate.format(projectName, projectModuleName);
        moduleLink.waitForVisible();
        moduleLink.click();
    }
}