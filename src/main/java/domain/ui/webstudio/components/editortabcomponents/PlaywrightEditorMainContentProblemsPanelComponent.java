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
        problemsPanel = new PlaywrightWebElement(page, ".//div[@id='editor-main-content-problems-panel']", "Problems Panel");
        errorsTab = new PlaywrightWebElement(page, ".//div[contains(@class,'tab') and contains(text(),'Errors')]", "Errors Tab");
        warningsTab = new PlaywrightWebElement(page, ".//div[contains(@class,'tab') and contains(text(),'Warnings')]", "Warnings Tab");
        closeBtn = new PlaywrightWebElement(page, ".//button[@title='Close'] | .//span[contains(@class,'close')]", "Close Button");
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