package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightDeployConfigurationTabsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement configurationTabs;
    private PlaywrightWebElement activeTab;
    private PlaywrightWebElement addConfigBtn;
    private PlaywrightWebElement saveBtn;

    public PlaywrightDeployConfigurationTabsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightDeployConfigurationTabsComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        configurationTabs = new PlaywrightWebElement(page, ".//div[contains(@class,'deploy-configuration-tabs')]", "Configuration Tabs");
        activeTab = new PlaywrightWebElement(page, ".//div[contains(@class,'tab-active')]", "Active Tab");
        addConfigBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Add Configuration']]", "Add Configuration Button");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save']]", "Save Button");
    }

    public void clickAddConfiguration() {
        addConfigBtn.click();
    }

    public void save() {
        saveBtn.click();
    }

    public boolean isConfigurationTabsVisible() {
        return configurationTabs.isVisible();
    }
}