package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

public class RightTableDetailsComponent extends BaseComponent {

    private WebElement addPropertyLink;
    private WebElement propertyTypeSelector;
    private WebElement addBtn;
    private WebElement cancelBtn;
    private WebElement saveBtn;
    private WebElement propertyInputTemplate;
    private WebElement propertyValueTemplate;

    // Property value reading (for inherited properties test)
    private WebElement propertyValueTextTemplate;
    private WebElement propertyRowTemplate;
    private WebElement goToPropertiesTableArrowTemplate;

    // Property rows list
    private List<WebElement> propertyRows;

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

        // Property value reading templates
        propertyValueTextTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[1]", "propertyValueText");
        propertyRowTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr[./td[contains(text(),'%s')]]", "propertyRow");
        goToPropertiesTableArrowTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[2]//a", "goToPropertiesTableArrow");

        // Property rows list
        propertyRows = createScopedElementList("xpath=.//div[@id='propsTable']//table[1]//tr[./td[@class='table-data-name']]", "propertyRows");
    }

    public void clickSaveBtn() {
        saveBtn.click();
        WaitUtil.sleep(500, "Waiting for table properties to be saved and UI to refresh");
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

    public String getPropertyValue(String propertyName) {
        return propertyValueTextTemplate.format(propertyName).getText().trim();
    }

    public WebElement getPropertyRow(String propertyName) {
        return propertyRowTemplate.format(propertyName);
    }

    public boolean isPropertyInherited(String propertyName) {
        String bgColor = getPropertyRow(propertyName).getCssValue("background-color");
        return bgColor.equals("rgba(190, 220, 255, 0.3)");
    }

    public String getPropertyRowBackgroundColor(String propertyName) {
        return getPropertyRow(propertyName).getCssValue("background-color");
    }

    public String getPropertyRowTitle(String propertyName) {
        return getPropertyRow(propertyName).getAttribute("title");
    }

    public WebElement getGoToPropertiesTableArrow(String propertyName) {
        return goToPropertiesTableArrowTemplate.format(propertyName);
    }

    public void clickGoToPropertiesTableArrow(String propertyName) {
        getGoToPropertiesTableArrow(propertyName).click();
        WaitUtil.sleep(500, "Waiting for Properties table to load after clicking arrow");
    }

    public String getGoToPropertiesTableArrowTitle(String propertyName) {
        return getGoToPropertiesTableArrow(propertyName).getAttribute("title");
    }

    public int getPropertiesRowCount() {
        WaitUtil.waitForCondition(() -> !propertyRows.isEmpty(), 2000, 100, "Waiting for property rows to load");
        return propertyRows.size();
    }

    public String getPropertyNameInRow(int rowIndex) {
        if (rowIndex < 1) {
            throw new IllegalArgumentException("Row index must be >= 1, got: " + rowIndex);
        }
        return propertyRows.get(rowIndex - 1).getText().trim();
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