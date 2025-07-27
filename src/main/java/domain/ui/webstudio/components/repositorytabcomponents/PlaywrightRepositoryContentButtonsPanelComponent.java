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

    public PlaywrightRepositoryContentButtonsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryContentButtonsPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        buttonsPanel = new PlaywrightWebElement(page, ".//div[contains(@class,'repository-buttons-panel')]", "Buttons Panel");
        deployBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Deploy']]", "Deploy Button");
        undeployBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Undeploy']]", "Undeploy Button");
        deleteBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Delete']]", "Delete Button");
        refreshBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Refresh']]", "Refresh Button");
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
}