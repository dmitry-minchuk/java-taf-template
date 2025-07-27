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
        projectNameLink = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']", "projectNameLink");
        projectModuleLink = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']/following-sibling::ul/li/a[text()='%s']", "projectModuleLink");
    }

    public void selectProject(String projectName) {
        projectNameLink.format(projectName).click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        selectProject(projectName);
        WaitUtil.sleep(200);
        PlaywrightWebElement formattedLink = projectModuleLink.format(projectName, projectModuleName);
        formattedLink.waitForVisible();
        formattedLink.click();
    }
}