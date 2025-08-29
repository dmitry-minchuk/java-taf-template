package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoryContentButtonsPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement buttonsPanel;
    private PlaywrightWebElement deployBtn;
    private PlaywrightWebElement undeployBtn;
    private PlaywrightWebElement deleteBtn;
    private PlaywrightWebElement refreshBtn;
    private PlaywrightWebElement openBtn;
    private PlaywrightWebElement saveBtn;

    public PlaywrightRepositoryContentButtonsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryContentButtonsPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        buttonsPanel = createScopedElement("xpath=.//div[contains(@class,'repository-buttons-panel')]", "buttonsPanel");
        deployBtn = createScopedElement("xpath=.//button[./span[text()='Deploy']]", "deployBtn");
        undeployBtn = createScopedElement("xpath=.//button[./span[text()='Undeploy']]", "undeployBtn");
        deleteBtn = createScopedElement("xpath=.//button[./span[text()='Delete']]", "deleteBtn");
        refreshBtn = createScopedElement("xpath=.//button[./span[text()='Refresh']]", "refreshBtn");
        openBtn = createScopedElement("xpath=.//input[@value='Open']", "openBtn");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save']] | //input[@value='Save']", "saveBtn");
    }

    public void clickDeploy() {
        deployBtn.click();
    }

    public void clickUndeploy() {
        undeployBtn.click();
    }

    public void clickDelete() {
        deleteBtn.click();
    }

    public void clickRefresh() {
        refreshBtn.click();
    }

    public boolean isButtonsPanelVisible() {
        return buttonsPanel.isVisible();
    }

    public void openProject() {
        openBtn.click();
    }

    public void saveDeploy() {
        saveBtn.click();
    }

    public boolean isDeployButtonEnabled() {
        return deployBtn.isEnabled();
    }
}