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
        configurationTabs = createScopedElement(".//div[contains(@class,'deploy-configuration-tabs')]", "configurationTabs");
        activeTab = createScopedElement(".//div[contains(@class,'tab-active')]", "activeTab");
        addConfigBtn = createScopedElement(".//button[./span[text()='Add Configuration']]", "addConfigBtn");
        saveBtn = createScopedElement(".//button[./span[text()='Save']]", "saveBtn");
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