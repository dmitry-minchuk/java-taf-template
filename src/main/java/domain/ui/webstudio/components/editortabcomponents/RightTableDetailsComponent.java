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

    // Editing properties templates
    private WebElement propertyValueLinkTemplate;
    private WebElement propertyTextInputTemplate;
    private WebElement propertyCheckboxInputTemplate;
    private WebElement propertyDropdownTemplate;
    private WebElement deletePropertyLinkTemplate;

    // Multi-select popup
    private WebElement selectAllCheckbox;
    private WebElement multiselectCheckboxTemplate;

    // Date picker calendar
    private WebElement calendarButtonTemplate;
    private WebElement calendarHeader;
    private WebElement calendarMonthTemplate;
    private WebElement calendarYearTemplate;
    private WebElement calendarOkButton;
    private WebElement calendarDayTemplate;

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
        propertyRowTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr[./td[normalize-space(text())='%s']]", "propertyRow");
        goToPropertiesTableArrowTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[2]//a", "goToPropertiesTableArrow");

        // Property rows list
        propertyRows = createScopedElementList("xpath=.//div[@id='propsTable']//table[1]//tr[./td[@class='table-data-name']]", "propertyRows");

        // New templates for editing
        propertyValueLinkTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[1]", "propertyValueLink");
        propertyTextInputTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[1]//input[@type='text']", "propertyTextInput");
        propertyCheckboxInputTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[1]//input[@type='checkbox']", "propertyCheckboxInput");
        propertyDropdownTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr/td[contains(text(),'%s')]/following-sibling::td[1]//select", "propertyDropdown");
        deletePropertyLinkTemplate = createScopedElement("xpath=.//div[@id='propsTable']//td[normalize-space(text())='%s']//..//td/a", "deletePropertyLink");

        // Multi-select popup
        selectAllCheckbox = createScopedElement("xpath=//div[@class='jquery-multiselect-popup jquery-popup']//label[text()='Select All']//..//input", "selectAllCheckbox");
        multiselectCheckboxTemplate = createScopedElement("xpath=//div[@class='jquery-multiselect-popup jquery-popup']//div[@class='jquery-multiselect-popup-data']//input[@value='%s']", "multiselectCheckbox");

        // Date picker calendar
        calendarButtonTemplate = createScopedElement("xpath=.//div[@id='propsTable']//tr/td[@class='propName' and contains(text(),'%s')]//..//img[@class='rf-cal-btn ']", "calendarButton");
        calendarHeader = createScopedElement("xpath=//div[@id='propsTable']//tr/td[@class='propName']//..//table[contains(@id,'dateContent')]//tr[1]/td[3]/div[contains(@onclick,'showDateEditor')]", "calendarHeader");
        calendarMonthTemplate = createScopedElement("xpath=//div//table[@class='rf-cal-monthpicker-cnt']//td//div[contains(text(),'%s')]", "calendarMonth");
        calendarYearTemplate = createScopedElement("xpath=//div//table[@class='rf-cal-monthpicker-cnt']//td//div[contains(text(),'%s')]", "calendarYear");
        calendarOkButton = createScopedElement("xpath=//div//table[@class='rf-cal-monthpicker-cnt']//td//div//span[contains(text(),'OK')]", "calendarOkButton");
        calendarDayTemplate = createScopedElement("xpath=//div[@id='propsTable']//td[contains(text(),'%s')]//..//..//table[contains(@id,'dateContent')]//tr/td[text()='%s' and not(contains(@class,'rf-cal-c-cnt-overflow'))]", "calendarDay");
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

    public void clickPropertyValue(String propertyName) {
        WaitUtil.sleep(300, "Waiting before clicking property value");
        propertyValueLinkTemplate.format(propertyName).click();
        WaitUtil.sleep(150, "Waiting after clicking property value");
    }

    public void editTextProperty(String propertyName, String newValue) {
        clickPropertyValue(propertyName);
        WebElement input = propertyTextInputTemplate.format(propertyName);
        input.clear();
        input.fillSequentially(newValue);
        WaitUtil.sleep(200, "Waiting after entering property value");
    }

    public void editBooleanProperty(String propertyName, boolean value) {
        clickPropertyValue(propertyName);
        WebElement checkbox = propertyCheckboxInputTemplate.format(propertyName);
        if (value) {
            checkbox.check();
        } else {
            checkbox.uncheck();
        }
    }

    public void editDropdownProperty(String propertyName, String value) {
        clickPropertyValue(propertyName);
        WebElement dropdown = propertyDropdownTemplate.format(propertyName);
        dropdown.selectByVisibleText(value);
    }

    public void editCheckboxProperty(String propertyName, String... values) {
        clickPropertyValue(propertyName);
        clickPropertyValue(propertyName);

        selectAllCheckbox.check();
        selectAllCheckbox.uncheck();

        for (String value : values) {
            WebElement checkbox = multiselectCheckboxTemplate.format(value);
            checkbox.check();
        }
    }

    public void editDateProperty(String propertyName, String dateValue) {
        clickPropertyValue(propertyName);
        openCalendarIfNeeded(propertyName);

        String[] dateParts = dateValue.split("/");
        int month = Integer.parseInt(dateParts[0]);
        String day = dateParts[1].replaceFirst("^0+(?!$)", "");
        String year = dateParts[2];

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String monthName = months[month - 1];

        selectMonthYearIfNeeded(monthName, year);
        selectDay(propertyName, day);
    }

    private void openCalendarIfNeeded(String propertyName) {
        WebElement input = propertyTextInputTemplate.format(propertyName);
        String value = input.getAttribute("value");
        if (value != null && !value.isEmpty() && !calendarHeader.isVisible()) {
            calendarButtonTemplate.format(propertyName).click();
            WaitUtil.sleep(200, "Waiting for calendar");
        }
    }

    private void selectMonthYearIfNeeded(String monthName, String year) {
        if (!calendarHeader.getText().trim().equals(monthName + ", " + year)) {
            calendarHeader.click();
            calendarMonthTemplate.format(monthName.substring(0, 3)).click();
            calendarYearTemplate.format(year).click();
            calendarOkButton.click();
        }
    }

    private void selectDay(String propertyName, String day) {
        calendarDayTemplate.format(propertyName, day).clickForce();
    }

    public void deleteProperty(String propertyName) {
        WebElement propertyRow = propertyRowTemplate.format(propertyName);
        propertyRow.hover();
        deletePropertyLinkTemplate.format(propertyName).click();
        WaitUtil.sleep(300, "Waiting after deleting property");
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