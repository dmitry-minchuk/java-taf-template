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

    public PlaywrightRepositoryContentButtonsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryContentButtonsPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        buttonsPanel = createScopedElement(".//div[contains(@class,'repository-buttons-panel')]", "buttonsPanel");
        deployBtn = createScopedElement(".//button[./span[text()='Deploy']]", "deployBtn");
        undeployBtn = createScopedElement(".//button[./span[text()='Undeploy']]", "undeployBtn");
        deleteBtn = createScopedElement(".//button[./span[text()='Delete']]", "deleteBtn");
        refreshBtn = createScopedElement(".//button[./span[text()='Refresh']]", "refreshBtn");
        openBtn = createScopedElement(".//button[./span[text()='Open']] | .//input[@value='Open']", "openBtn");
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

    public boolean isDeployButtonEnabled() {
        return deployBtn.isEnabled();
    }

    public void openProject() {
        openBtn.click();
    }

    public boolean isOpenButtonEnabled() {
        return openBtn.isEnabled();
    }
}