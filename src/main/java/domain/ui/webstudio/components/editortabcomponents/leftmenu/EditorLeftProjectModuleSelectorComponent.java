package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.editortabcomponents.ProblemsPanelComponent;
import helpers.utils.WaitUtil;

import java.util.List;


// Handles clicking on project names and specific modules within projects
public class EditorLeftProjectModuleSelectorComponent extends BaseComponent {

    private WebElement projectNameTemplate;
    private WebElement projectModuleTemplate;
    private ProblemsPanelComponent problemsPanelComponent;

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
        problemsPanelComponent = new ProblemsPanelComponent(new WebElement(page, "xpath=//div[@id='bottom']"));
    }

    public void selectProject(String projectName) {
        WaitUtil.retryOnException(() -> {
            projectNameTemplate.format(projectName).click(50);
            return true;
        }, 5000, 250, "Trying to select " + projectName + " project...");
    }

    public void selectModule(String projectName, String projectModuleName) {
        waitUntilSpinnerLoaded();
        selectProject(projectName);
        WaitUtil.sleep(200, "Waiting for module list to expand after project selection");
        WebElement moduleLink = projectModuleTemplate.format(projectName, projectModuleName);
        moduleLink.waitForVisible();
        moduleLink.click();
        WaitUtil.sleep(200, "Waiting for module to open");
        problemsPanelComponent.waitForCompilationToComplete();
    }

    public List<String> getAllModuleNames(String projectName) {
        selectProject(projectName);
        WaitUtil.sleep(200, "Waiting for module list to load after project selection");

        // Get modules ONLY for the selected project using targeted XPath
        String moduleXPath = String.format("xpath=.//li/a[@class='projectName' and text()='%s']/following-sibling::ul/li/a", projectName);
        List<WebElement> projectModules = createScopedElementList(moduleXPath, "modulesFor_" + projectName);

        return projectModules.stream()
                .map(WebElement::getText)
                .filter(text -> !text.isEmpty())
                .toList();
    }
}