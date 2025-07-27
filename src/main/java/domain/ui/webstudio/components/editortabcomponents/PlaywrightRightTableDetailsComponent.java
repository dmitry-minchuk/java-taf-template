package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

// Playwright version of RightTableDetailsComponent for property management
public class PlaywrightRightTableDetailsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement addPropertyLink;
    private PlaywrightWebElement propertyTypeSelector;
    private PlaywrightWebElement addBtn;
    private PlaywrightWebElement cancelBtn;
    private PlaywrightWebElement propertyInputTextField;
    private PlaywrightWebElement propertyContent;
    @Getter
    private PlaywrightWebElement saveBtn;

    public PlaywrightRightTableDetailsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRightTableDetailsComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Add property link: ".//a[@id='addPropBtn']"
        addPropertyLink = createScopedElement("xpath=.//a[@id='addPropBtn']", "addPropertyLink");
        
        // Property type selector dropdown: ".//div[@id='addPropsPanel']//select"
        propertyTypeSelector = createScopedElement("xpath=.//div[@id='addPropsPanel']//select", "propertyTypeSelector");
        
        // Add button: ".//div[@id='addPropsPanel']//input[@value='Add']"
        addBtn = createScopedElement("xpath=.//div[@id='addPropsPanel']//input[@value='Add']", "addBtn");
        
        // Cancel button: ".//div[@id='addPropsPanel']//a[text()='Cancel']"
        cancelBtn = createScopedElement("xpath=.//div[@id='addPropsPanel']//a[text()='Cancel']", "cancelBtn");
        
        // Property input text field (dynamic with property name): ".//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span/input"
        propertyInputTextField = createScopedElement("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span/input", "propertyInputTextField");
        
        // Property content validation (dynamic with property name and value): ".//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span[1][contains(text(),'%s')]"
        propertyContent = createScopedElement("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span[1][contains(text(),'%s')]", "propertyContent");
        
        // Save button: ".//input[@id='savePropsButton']"
        saveBtn = createScopedElement("xpath=.//input[@id='savePropsButton']", "saveBtn");
    }

    public PlaywrightRightTableDetailsComponent addProperty(String propertyName) {
        addPropertyLink.click();
        propertyTypeSelector.selectByVisibleText(propertyName);
        addBtn.click();
        return this;
    }

    public PlaywrightRightTableDetailsComponent setProperty(String propertyName, String propertyValue) {
        String selector = String.format("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span/input", propertyName);
        createScopedElement(selector, "propertyInputTextField").fill(propertyValue);
        return this;
    }

    public boolean isPropertySet(String propertyName, String propertyValue) {
        try {
            String selector = String.format("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span[1][contains(text(),'%s')]", propertyName, propertyValue);
            createScopedElement(selector, "propertyContent").waitForVisible();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Getter
    public enum DropdownOptions {
        DESCRIPTION("Description"),
        CATEGORY("Category"),
        TAGS("Tags");

        private String value;

        DropdownOptions(String value) {
            this.value = value;
        }
    }
}