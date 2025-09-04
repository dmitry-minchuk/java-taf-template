package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;


// Handles clicking on project names and specific modules within projects
public class EditorLeftProjectModuleSelectorComponent extends BaseComponent {

    private WebElement projectNameTemplate;
    private WebElement projectModuleTemplate;

    public EditorLeftProjectModuleSelectorComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorLeftProjectModuleSelectorComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        projectNameTemplate = createScopedElement("xpath=.//li/a[@class='projectName' and text()='%s']", "projectNameLink");
        projectModuleTemplate = createScopedElement("xpath=.//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']", "projectModuleLink");
    }

    public void selectProject(String projectName) {
        projectNameTemplate.format(projectName).waitForVisible(DEFAULT_TIMEOUT_MS).click();
    }

    public void selectModule(String projectName, String projectModuleName) {
        selectProject(projectName);
        WaitUtil.sleep(200);
        WebElement moduleLink = projectModuleTemplate.format(projectName, projectModuleName);
        moduleLink.waitForVisible();
        moduleLink.click();
    }
}