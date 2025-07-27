package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoryContentTabPropertiesComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement propertiesTab;
    private PlaywrightWebElement propertyName;
    private PlaywrightWebElement propertyValue;
    private PlaywrightWebElement addPropertyBtn;
    private PlaywrightWebElement saveBtn;

    public PlaywrightRepositoryContentTabPropertiesComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryContentTabPropertiesComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        propertiesTab = createScopedElement(".//div[contains(@class,'properties-tab')]", "propertiesTab");
        propertyName = createScopedElement(".//input[@placeholder='Property Name']", "propertyName");
        propertyValue = createScopedElement(".//input[@placeholder='Property Value']", "propertyValue");
        addPropertyBtn = createScopedElement(".//button[./span[text()='Add Property']]", "addPropertyBtn");
        saveBtn = createScopedElement(".//button[./span[text()='Save']]", "saveBtn");
    }

    public void setPropertyName(String name) {
        propertyName.fill(name);
    }

    public void setPropertyValue(String value) {
        propertyValue.fill(value);
    }

    public void addProperty() {
        addPropertyBtn.click();
    }

    public void save() {
        saveBtn.click();
    }

    public boolean isPropertiesTabVisible() {
        return propertiesTab.isVisible();
    }
}