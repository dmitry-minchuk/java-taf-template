package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightEditorMainContentProblemsPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement problemsPanel;
    private PlaywrightWebElement errorsTab;
    private PlaywrightWebElement warningsTab;
    private PlaywrightWebElement closeBtn;

    public PlaywrightEditorMainContentProblemsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightEditorMainContentProblemsPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        problemsPanel = createScopedElement(".//div[@id='editor-main-content-problems-panel']", "problemsPanel");
        errorsTab = createScopedElement(".//div[contains(@class,'tab') and contains(text(),'Errors')]", "errorsTab");
        warningsTab = createScopedElement(".//div[contains(@class,'tab') and contains(text(),'Warnings')]", "warningsTab");
        closeBtn = createScopedElement(".//button[@title='Close'] | .//span[contains(@class,'close')]", "closeBtn");
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
}