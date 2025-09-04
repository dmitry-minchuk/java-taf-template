package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

// Playwright version of RightTableDetailsComponent for property management
public class RightTableDetailsComponent extends BaseComponent {

    private WebElement addPropertyLink;
    private WebElement propertyTypeSelector;
    private WebElement addBtn;
    private WebElement cancelBtn;
    private WebElement saveBtn;
    private WebElement propertyInputTemplate;
    private WebElement propertyValueTemplate;

    public RightTableDetailsComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RightTableDetailsComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        addPropertyLink = createScopedElement("xpath=.//a[@id='addPropBtn']", "addPropertyLink");
        propertyTypeSelector = createScopedElement("xpath=.//div[@id='addPropsPanel']//select", "propertyTypeSelector");
        addBtn = createScopedElement("xpath=.//div[@id='addPropsPanel']//input[@value='Add']", "addBtn");
        cancelBtn = createScopedElement("xpath=.//div[@id='addPropsPanel']//a[text()='Cancel']", "cancelBtn");
        saveBtn = createScopedElement("xpath=.//input[@id='savePropsButton']", "saveBtn");
        propertyInputTemplate = createScopedElement("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span/input", "propertyInputField");
        propertyValueTemplate = createScopedElement("xpath=.//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span[1][contains(text(),'%s')]", "propertyValueCell");
    }

    public void clickSaveBtn() {
        WaitUtil.sleep(100);
        saveBtn.click();
    }

    public RightTableDetailsComponent addProperty(String propertyName) {
        addPropertyLink.click();
        propertyTypeSelector.selectByVisibleText(propertyName);
        addBtn.click();
        return this;
    }

    public RightTableDetailsComponent setProperty(String propertyName, String propertyValue) {
        propertyInputTemplate.format(propertyName).fillSequentially(propertyValue);
        return this;
    }

    public boolean isPropertySet(String propertyName, String propertyValue) {
        try {
            WebElement propertyValueCell = propertyValueTemplate.format(propertyName, propertyValue);
            propertyValueCell.waitForVisible();
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