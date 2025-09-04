package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class EditorMainContentProblemsPanelComponent extends CoreComponent {

    private WebElement problemsPanel;
    private WebElement errorsTab;
    private WebElement warningsTab;
    private WebElement closeBtn;
    private List<WebElement> errorMessages;

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
    }

    public void clickErrorsTab() {
        errorsTab.click();
    }

    public void clickWarningsTab() {
        warningsTab.click();
    }

    public void closePanel() {
        closeBtn.click();
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

    public boolean isErrorMessagePresent() {
        return WaitUtil.isListNotEmpty(() -> errorMessages, 5000, 250);
    }
}