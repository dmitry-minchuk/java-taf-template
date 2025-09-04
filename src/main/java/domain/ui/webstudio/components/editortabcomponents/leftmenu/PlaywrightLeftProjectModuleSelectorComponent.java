package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;


// Handles clicking on project names and specific modules within projects
public class PlaywrightLeftProjectModuleSelectorComponent extends CoreComponent {

    private PlaywrightWebElement projectNameTemplate;
    private PlaywrightWebElement projectModuleTemplate;

    public PlaywrightLeftProjectModuleSelectorComponent() {
        super(LocalDriverPool.getPage());
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
        projectNameTemplate.format(projectName).waitForVisible().click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        selectProject(projectName);
        WaitUtil.sleep(200);
        PlaywrightWebElement moduleLink = projectModuleTemplate.format(projectName, projectModuleName);
        moduleLink.waitForVisible();
        moduleLink.click();
    }
}