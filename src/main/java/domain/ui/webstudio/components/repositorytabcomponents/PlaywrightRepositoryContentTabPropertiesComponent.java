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
        propertiesTab = new PlaywrightWebElement(page, ".//div[contains(@class,'properties-tab')]", "Properties Tab");
        propertyName = new PlaywrightWebElement(page, ".//input[@placeholder='Property Name']", "Property Name");
        propertyValue = new PlaywrightWebElement(page, ".//input[@placeholder='Property Value']", "Property Value");
        addPropertyBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Add Property']]", "Add Property Button");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save']]", "Save Button");
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