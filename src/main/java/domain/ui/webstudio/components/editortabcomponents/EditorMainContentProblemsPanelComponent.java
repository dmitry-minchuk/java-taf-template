package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class EditorMainContentProblemsPanelComponent extends BaseComponent {

    private WebElement problemsPanel;
    private WebElement errorsTab;
    private WebElement warningsTab;
    private WebElement closeBtn;

    private List<WebElement> errorMessages;
    private WebElement hideProblemsBtn;
    private WebElement showProblemsBtn;

    public EditorMainContentProblemsPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorMainContentProblemsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        problemsPanel = createScopedElement("xpath=.//div[@id='editor-main-content-problems-panel']", "problemsPanel");
        errorsTab = createScopedElement("xpath=.//div[contains(@class,'tab') and contains(text(),'Errors')]", "errorsTab");
        warningsTab = createScopedElement("xpath=.//div[contains(@class,'tab') and contains(text(),'Warnings')]", "warningsTab");
        closeBtn = createScopedElement("xpath=.//button[@title='Close'] | .//span[contains(@class,'close')]", "closeBtn");

        errorMessages = createScopedElementList("xpath=.//div[@class='problem-error']", "errorMessages");
        hideProblemsBtn = createScopedElement("xpath=.//img[@title='Hide Problems']", "hideProblemsBtn");
        showProblemsBtn = createScopedElement("xpath=.//img[@title='Show Problems']", "showProblemsBtn");
    }

    public EditorMainContentProblemsPanelComponent clickErrorsTab() {
        errorsTab.click();
        return this;
    }

    public EditorMainContentProblemsPanelComponent clickWarningsTab() {
        warningsTab.click();
        return this;
    }

    public EditorMainContentProblemsPanelComponent closePanel() {
        closeBtn.click();
        return this;
    }

    public boolean isProblemsPanelVisible() {
        return problemsPanel.isVisible();
    }

    public boolean isErrorsTabActive() {
        return errorsTab.getAttribute("class").contains("active");
    }

    public boolean isWarningsTabActive() {
        return warningsTab.getAttribute("class").contains("active");
    }

    public EditorMainContentProblemsPanelComponent clickHideProblemsBtn() {
        hideProblemsBtn.click();
        return this;
    }

    public EditorMainContentProblemsPanelComponent clickShowProblemsBtn() {
        showProblemsBtn.click();
        return this;
    }

    public EditorMainContentProblemsPanelComponent expandProblemDescription(int elementPosition) {
        WaitUtil.waitForListNotEmpty(() -> errorMessages, 10000, 250, "Waiting for error messages before expanding");
        WaitUtil.sleep(200, "Waiting for error messages panel to stabilize");
        errorMessages.get(elementPosition).getLocator().locator("xpath=.//div[@class='stacktrace-hidden']").click();
        return this;
    }

    public EditorMainContentProblemsPanelComponent hideProblemDescription(int elementPosition) {
        WaitUtil.waitForListNotEmpty(() -> errorMessages, 10000, 250, "Waiting for error messages before hiding");
        WaitUtil.sleep(200, "Waiting for error messages panel to stabilize");
        errorMessages.get(elementPosition).getLocator().locator("xpath=.//div[@class='arrow-top']//div[@class='stacktrace-showed']").click();
        return this;
    }

    public boolean isProblemDescriptionVisible(int elementPosition) {
        return WaitUtil.waitForCondition(() -> errorMessages.get(elementPosition).getLocator().locator("xpath=.//span[@class='stacktrace-panels']").isVisible(), 1000, 100, "Waiting for ProblemDescription to be visible...");
    }

    public boolean isErrorMessageListPresent() {
        return WaitUtil.isListNotEmpty(() -> errorMessages, 10000, 250, "Waiting for error messages to appear in problems panel");
    }
}