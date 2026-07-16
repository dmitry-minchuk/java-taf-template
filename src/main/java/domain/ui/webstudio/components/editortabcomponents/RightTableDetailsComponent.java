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
    private WebElement loadedTableNameTemplate;

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

    // RichFaces calendar popup (readonly date inputs are picked via the widget, scoped to the one open popup)
    private WebElement calMonthLabel;
    private WebElement calPrevYear;
    private WebElement calNextYear;
    private WebElement calPrevMonth;
    private WebElement calNextMonth;
    private WebElement calDayTemplate;

    private static final List<String> MONTHS = List.of("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");

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
        loadedTableNameTemplate = createScopedElement("xpath=.//div[@id='propsTable']//table[1]//tr[td[normalize-space()='Name']]/td[normalize-space()='%s']", "loadedTableName");

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

        // RichFaces calendar: scope to the single open popup (.rf-cal-popup:visible); nav cells and days by text.
        String visiblePopup = ".rf-cal-popup:visible >> ";
        calMonthLabel = new WebElement(getPage(), visiblePopup + "css=.rf-cal-hdr-month", "calMonthLabel");
        calPrevYear = new WebElement(getPage(), visiblePopup + "xpath=.//td[contains(@class,'rf-cal-tl') and normalize-space(.)='<<']", "calPrevYear");
        calNextYear = new WebElement(getPage(), visiblePopup + "xpath=.//td[contains(@class,'rf-cal-tl') and normalize-space(.)='>>']", "calNextYear");
        calPrevMonth = new WebElement(getPage(), visiblePopup + "xpath=.//td[contains(@class,'rf-cal-tl') and normalize-space(.)='<']", "calPrevMonth");
        calNextMonth = new WebElement(getPage(), visiblePopup + "xpath=.//td[contains(@class,'rf-cal-tl') and normalize-space(.)='>']", "calNextMonth");
        calDayTemplate = new WebElement(getPage(), visiblePopup
                + "xpath=.//td[(contains(@class,'rf-cal-btn') or contains(@class,'rf-cal-sel') or contains(@class,'rf-cal-today'))"
                + " and not(contains(@class,'rf-cal-boundary-day')) and normalize-space(.)='%s']", "calDay");

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

    // Panel reloads asynchronously after table selection; wait until it reflects the target table
    public RightTableDetailsComponent waitForTableLoaded(String tableName) {
        loadedTableNameTemplate.format(tableName).waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
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

    // The date property is a readonly RichFaces calendar, so the value is picked through the widget: open the
    // popup, navigate to the target month/year, and click the day (which commits "MM/DD/YYYY 12:00 AM" and closes).
    public void editDateProperty(String propertyName, String dateValue) {
        clickPropertyValue(propertyName);
        propertyTextInputTemplate.format(propertyName).click();
        calMonthLabel.waitForVisible(DEFAULT_TIMEOUT_MS);
        String[] parts = dateValue.split("/");
        int targetMonth = Integer.parseInt(parts[0]);
        int targetDay = Integer.parseInt(parts[1]);
        int targetYear = Integer.parseInt(parts[2]);
        navigateCalendarTo(targetMonth, targetYear);
        calDayTemplate.format(String.valueOf(targetDay)).click();
        WaitUtil.sleep(200, "Waiting after picking the date");
    }

    private void navigateCalendarTo(int targetMonth, int targetYear) {
        int targetIndex = targetYear * 12 + (targetMonth - 1);
        for (int guard = 0; guard < 240; guard++) {
            int[] current = readCalendarMonthYear();
            int currentIndex = current[1] * 12 + (current[0] - 1);
            if (currentIndex == targetIndex) {
                return;
            }
            if (currentIndex > targetIndex) {
                (current[1] > targetYear ? calPrevYear : calPrevMonth).click();
            } else {
                (current[1] < targetYear ? calNextYear : calNextMonth).click();
            }
            WaitUtil.sleep(150, "Calendar navigation step");
        }
        throw new IllegalStateException("Calendar did not reach " + targetMonth + "/" + targetYear);
    }

    // Reads the popup header label "Month, YYYY" (e.g. "July, 2026") into [month(1-12), year].
    private int[] readCalendarMonthYear() {
        String[] label = calMonthLabel.getText().trim().split(",");
        return new int[]{MONTHS.indexOf(label[0].trim()) + 1, Integer.parseInt(label[1].trim())};
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
