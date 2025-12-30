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
    private List<WebElement> moduleElements;
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
        moduleElements = createScopedElementList("xpath=.//ul/li/a[contains(@class,'module') or not(@class)]", "moduleElements");
        problemsPanelComponent = new ProblemsPanelComponent(new WebElement(page, "xpath=//div[@id='bottom']"));
    }

    public void selectProject(String projectName) {
        projectNameTemplate.format(projectName).waitForVisible(DEFAULT_TIMEOUT_MS).click();
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
        return moduleElements.stream()
                .filter(WebElement::isVisible)
                .map(WebElement::getText)
                .filter(text -> !text.isEmpty())
                .toList();
    }
}