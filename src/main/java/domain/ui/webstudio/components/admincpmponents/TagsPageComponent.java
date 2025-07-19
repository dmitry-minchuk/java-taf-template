package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagsPageComponent extends BasePageComponent {

    // Add Tag Section Elements
    @FindBy(xpath = ".//button[./span[text()='Add Tag'] or ./span[contains(text(),'Add')]]")
    private SmartWebElement addTagBtn;

    @FindBy(xpath = ".//input[@placeholder='Tag Name' or @id='tagName']")
    private SmartWebElement tagNameField;

    @FindBy(xpath = ".//input[@placeholder='Tag Description' or @id='tagDescription']")
    private SmartWebElement tagDescriptionField;

    @FindBy(xpath = ".//input[@placeholder='Tag Color' or @id='tagColor']")
    private SmartWebElement tagColorField;

    @FindBy(xpath = ".//div[contains(@class,'color-picker') or contains(@class,'ant-color-picker')]")
    private SmartWebElement colorPicker;

    // Tag Category Section
    @FindBy(xpath = ".//select[contains(@id,'tagCategory')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Category')]]")
    private SmartWebElement tagCategoryDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Business Rules')]")
    private SmartWebElement businessRulesCategoryOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Data Models')]")
    private SmartWebElement dataModelsCategoryOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Projects')]")
    private SmartWebElement projectsCategoryOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'General')]")
    private SmartWebElement generalCategoryOption;

    @FindBy(xpath = ".//input[@placeholder='Custom Category' or @id='customCategory']")
    private SmartWebElement customCategoryField;

    // Tag Priority and Visibility
    @FindBy(xpath = ".//select[contains(@id,'tagPriority')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Priority')]]")
    private SmartWebElement tagPriorityDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'High')]")
    private SmartWebElement highPriorityOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Medium')]")
    private SmartWebElement mediumPriorityOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Low')]")
    private SmartWebElement lowPriorityOption;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'publicTag') or ./following-sibling::*[contains(text(),'Public Tag')])]")
    private SmartWebElement publicTagCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'systemTag') or ./following-sibling::*[contains(text(),'System Tag')])]")
    private SmartWebElement systemTagCheckbox;

    // Tags Table Elements
    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']")
    private SmartWebElement tagsTableBody;

    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']//tr[@class='ant-table-row ant-table-row-level-0']")
    private List<SmartWebElement> tagRows;

    // Tag Row Template Elements (for dynamic tag interaction)
    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]")
    private SmartWebElement tagRowTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//td[1]//span[contains(@class,'tag')]")
    private SmartWebElement tagDisplayTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//td[2]")
    private SmartWebElement tagDescriptionTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//td[3]")
    private SmartWebElement tagCategoryTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//td[4]")
    private SmartWebElement tagUsageCountTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//button[contains(@class,'edit') or ./span[contains(@aria-label,'edit')]]")
    private SmartWebElement editTagBtnTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//button[contains(@class,'delete') or ./span[contains(@aria-label,'delete')]]")
    private SmartWebElement deleteTagBtnTemplate;

    @FindBy(xpath = ".//tr[contains(@data-row-key,'%s') or .//td[contains(text(),'%s')]]//button[contains(@class,'copy') or ./span[contains(@aria-label,'copy')]]")
    private SmartWebElement duplicateTagBtnTemplate;

    // Tag Form Modal Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Tag Name']")
    private SmartWebElement modalTagNameField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Description']")
    private SmartWebElement modalTagDescriptionField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//button[./span[text()='Save Tag'] or ./span[text()='Save']]")
    private SmartWebElement saveTagBtn;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//button[./span[text()='Cancel']]")
    private SmartWebElement cancelTagBtn;

    // Search and Filter Elements
    @FindBy(xpath = ".//input[@placeholder='Search tags...' or contains(@class,'search')]")
    private SmartWebElement searchTagsField;

    @FindBy(xpath = ".//select[contains(@id,'filterCategory')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Filter by Category')]]")
    private SmartWebElement filterCategoryDropdown;

    @FindBy(xpath = ".//select[contains(@id,'filterPriority')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Filter by Priority')]]")
    private SmartWebElement filterPriorityDropdown;

    @FindBy(xpath = ".//button[./span[text()='Clear Filters'] or ./span[contains(text(),'Clear')]]")
    private SmartWebElement clearFiltersBtn;

    // Bulk Operations Elements
    @FindBy(xpath = ".//input[@type='checkbox' and contains(@class,'select-all')]")
    private SmartWebElement selectAllTagsCheckbox;

    @FindBy(xpath = ".//button[./span[text()='Bulk Delete'] or ./span[contains(text(),'Delete Selected')]]")
    private SmartWebElement bulkDeleteBtn;

    @FindBy(xpath = ".//button[./span[text()='Bulk Export'] or ./span[contains(text(),'Export Selected')]]")
    private SmartWebElement bulkExportBtn;

    @FindBy(xpath = ".//button[./span[text()='Import Tags'] or ./span[contains(text(),'Import')]]")
    private SmartWebElement importTagsBtn;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Save Changes'] or ./span[text()='Apply']]")
    private SmartWebElement saveChangesBtn;

    @FindBy(xpath = ".//button[./span[text()='Reset Tags'] or ./span[text()='Reset']]")
    private SmartWebElement resetTagsBtn;

    // Status and Notification Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]")
    private SmartWebElement successNotification;

    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]")
    private SmartWebElement errorNotification;

    // Tag Statistics Elements
    @FindBy(xpath = ".//div[contains(@class,'stats-card')]//span[contains(@class,'total-tags')]")
    private SmartWebElement totalTagsCount;

    @FindBy(xpath = ".//div[contains(@class,'stats-card')]//span[contains(@class,'active-tags')]")
    private SmartWebElement activeTagsCount;

    @FindBy(xpath = ".//div[contains(@class,'stats-card')]//span[contains(@class,'unused-tags')]")
    private SmartWebElement unusedTagsCount;

    // Optimized category and priority mappings
    private final Map<String, SmartWebElement> categoryMappings = new HashMap<>();
    private final Map<String, SmartWebElement> priorityMappings = new HashMap<>();

    public TagsPageComponent() {
        super();
        initializeMappings();
    }

    private void initializeMappings() {
        // Initialize category mappings
        categoryMappings.put("business rules", businessRulesCategoryOption);
        categoryMappings.put("data models", dataModelsCategoryOption);
        categoryMappings.put("projects", projectsCategoryOption);
        categoryMappings.put("general", generalCategoryOption);
        
        // Initialize priority mappings
        priorityMappings.put("high", highPriorityOption);
        priorityMappings.put("medium", mediumPriorityOption);
        priorityMappings.put("low", lowPriorityOption);
    }


    // Tag Management Methods
    public void clickAddTag() {
        addTagBtn.click();
    }

    public int getTagCount() {
        return tagRows.size();
    }

    public boolean isTagExists(String tagName) {
        return tagRowTemplate.format(tagName, tagName).isDisplayed(2);
    }

    public String getTagDescription(String tagName) {
        return tagDescriptionTemplate.format(tagName, tagName).getText();
    }

    public String getTagCategory(String tagName) {
        return tagCategoryTemplate.format(tagName, tagName).getText();
    }

    public int getTagUsageCount(String tagName) {
        String usageText = tagUsageCountTemplate.format(tagName, tagName).getText();
        return usageText.matches("\\d+") ? Integer.parseInt(usageText) : 0;
    }

    // Tag Actions
    public void clickEditTag(String tagName) {
        editTagBtnTemplate.format(tagName, tagName).click();
    }

    public void clickDeleteTag(String tagName) {
        deleteTagBtnTemplate.format(tagName, tagName).click();
    }

    public void clickDuplicateTag(String tagName) {
        duplicateTagBtnTemplate.format(tagName, tagName).click();
    }

    // Tag Form Operations
    public void fillTagForm(String name, String description, String category, String priority, boolean isPublic) {
        if (name != null) {
            if (modalTagNameField.isDisplayed(2)) {
                modalTagNameField.sendKeys(name);
            } else {
                tagNameField.sendKeys(name);
            }
        }
        
        if (description != null) {
            if (modalTagDescriptionField.isDisplayed(2)) {
                modalTagDescriptionField.sendKeys(description);
            } else {
                tagDescriptionField.sendKeys(description);
            }
        }
        
        if (category != null) {
            setTagCategory(category);
        }
        
        if (priority != null) {
            setTagPriority(priority);
        }
        
        setPublicTag(isPublic);
    }

    public void setTagCategory(String category) {
        tagCategoryDropdown.click();
        
        SmartWebElement categoryElement = getCategoryElement(category);
        if (categoryElement != null) {
            categoryElement.click();
        } else {
            // Custom category
            customCategoryField.sendKeys(category);
        }
    }

    private SmartWebElement getCategoryElement(String category) {
        return categoryMappings.get(category.toLowerCase());
    }

    public void setTagPriority(String priority) {
        tagPriorityDropdown.click();
        
        SmartWebElement priorityElement = getPriorityElement(priority);
        if (priorityElement != null) {
            priorityElement.click();
        }
    }

    private SmartWebElement getPriorityElement(String priority) {
        return priorityMappings.get(priority.toLowerCase());
    }

    public void setPublicTag(boolean isPublic) {
        if (isPublic != publicTagCheckbox.isSelected()) {
            publicTagCheckbox.click();
        }
    }

    public void setSystemTag(boolean isSystem) {
        if (isSystem != systemTagCheckbox.isSelected()) {
            systemTagCheckbox.click();
        }
    }

    public void saveTag() {
        saveTagBtn.click();
    }

    public void cancelTag() {
        cancelTagBtn.click();
    }

    // Search and Filter Operations
    public void searchTags(String searchTerm) {
        searchTagsField.sendKeys(searchTerm);
    }

    public void filterByCategory(String category) {
        filterCategoryDropdown.click();
        // Click on the category option (implementation would depend on actual options)
    }

    public void filterByPriority(String priority) {
        filterPriorityDropdown.click();
        // Click on the priority option (implementation would depend on actual options)
    }

    public void clearFilters() {
        clearFiltersBtn.click();
    }

    // Bulk Operations
    public void selectAllTags() {
        selectAllTagsCheckbox.click();
    }

    public void bulkDeleteSelected() {
        bulkDeleteBtn.click();
        getConfirmationPopup().confirm();
    }

    public void bulkExportSelected() {
        bulkExportBtn.click();
    }

    public void importTags() {
        importTagsBtn.click();
    }

    // Action Methods
    public void saveChanges() {
        saveChangesBtn.click();
    }

    public void resetTags() {
        resetTagsBtn.click();
        getConfirmationPopup().confirm();
    }

    // Status and Statistics Methods
    public int getTotalTagsCount() {
        String countText = totalTagsCount.getText();
        return countText.matches("\\d+") ? Integer.parseInt(countText) : 0;
    }

    public int getActiveTagsCount() {
        String countText = activeTagsCount.getText();
        return countText.matches("\\d+") ? Integer.parseInt(countText) : 0;
    }

    public int getUnusedTagsCount() {
        String countText = unusedTagsCount.getText();
        return countText.matches("\\d+") ? Integer.parseInt(countText) : 0;
    }

    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isDisplayed(3);
    }

    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isDisplayed(3);
    }

    // Complex Tag Operations
    public void createTag(String name, String description, String category, String priority, boolean isPublic) {
        clickAddTag();
        fillTagForm(name, description, category, priority, isPublic);
        saveTag();
    }

    public void createSimpleTag(String name, String description) {
        createTag(name, description, "General", "Medium", true);
    }

    public void editTag(String tagName, String newDescription, String newCategory) {
        clickEditTag(tagName);
        fillTagForm(null, newDescription, newCategory, null, true);
        saveTag();
    }

    public void deleteTag(String tagName) {
        clickDeleteTag(tagName);
        getConfirmationPopup().confirm();
    }

    public void duplicateTag(String originalTagName, String newTagName) {
        clickDuplicateTag(originalTagName);
        fillTagForm(newTagName, null, null, null, true);
        saveTag();
    }

    // Tag Validation Methods
    public boolean validateTag(String tagName, String expectedDescription, String expectedCategory) {
        if (!isTagExists(tagName)) {
            return false;
        }
        
        boolean descriptionMatches = expectedDescription == null || expectedDescription.equals(getTagDescription(tagName));
        boolean categoryMatches = expectedCategory == null || expectedCategory.equals(getTagCategory(tagName));
        
        return descriptionMatches && categoryMatches;
    }

    public String getTagInfo(String tagName) {
        if (!isTagExists(tagName)) {
            return "Tag not found: " + tagName;
        }
        
        return String.format("Tag: %s | Description: %s | Category: %s | Usage: %d",
                tagName,
                getTagDescription(tagName),
                getTagCategory(tagName),
                getTagUsageCount(tagName));
    }

    public String getTagsStatistics() {
        return String.format("Tags Statistics - Total: %d | Active: %d | Unused: %d | In Table: %d",
                getTotalTagsCount(),
                getActiveTagsCount(),
                getUnusedTagsCount(),
                getTagCount());
    }
}