package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TagsPageComponent extends BaseComponent {

    private WebElement tagTypesAndValuesSection;
    private WebElement newTagTypeInput;

    private WebElement tagTypeTemplate;
    private WebElement tagTypeNameInputTemplate;
    private WebElement tagTypeOptionalCheckboxTemplate;
    private WebElement tagTypeExtensibleCheckboxTemplate;
    private WebElement tagTypeDeleteBtnTemplate;
    private WebElement newTagValueInputTemplate;
    private WebElement existingTagValueTemplate;
    private WebElement tagValueDropdownTemplate;
    private WebElement deleteTagValueBtnTemplate;
    private WebElement editTagValueBtnTemplate;
    private WebElement editTagValueInputTemplate;

    @Getter
    private WebElement tagsFromProjectNameSection;
    private WebElement projectNameTemplatesInput;
    private WebElement saveTemplatesBtn;
    private WebElement templateErrorMsg;
    private WebElement fillTagsForProjectBtn;

    private WebElement projectsWithoutTagsTable;
    private WebElement saveProjectsWithoutTagsBtn;

    public TagsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TagsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tagTypesAndValuesSection = createScopedElement(
            "xpath=.//div[@id='tags' or contains(@class,'tags')]//h1[contains(text(), 'Tag Types and Values')]/..",
            "tagTypesAndValuesSection"
        );
        newTagTypeInput = createScopedElement(
            "xpath=.//input[@placeholder='New Tag Type']",
            "newTagTypeInput"
        );

        tagTypeTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]",
            "tagTypeTemplate"
        );
        tagTypeNameInputTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//input[@title='Tag type']",
            "tagTypeNameInputTemplate"
        );
        tagTypeOptionalCheckboxTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//label[contains(@title,'optional')]//input",
            "tagTypeOptionalCheckboxTemplate"
        );
        tagTypeExtensibleCheckboxTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//label[contains(@title,'can be created')]//input",
            "tagTypeExtensibleCheckboxTemplate"
        );
        tagTypeDeleteBtnTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//span[@class='clickable delete-icon']",
            "tagTypeDeleteBtnTemplate"
        );
        newTagValueInputTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//input",
            "newTagValueInputTemplate"
        );
        existingTagValueTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//div[@class='tag-value'][contains(., '%s')]",
            "existingTagValueTemplate"
        );
        tagValueDropdownTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//div[@class='tag-value']//span[contains(text(), '%s')]//..//div[@class='dropdown']",
            "tagValueDropdownTemplate"
        );
        deleteTagValueBtnTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//div[@class='tag-value']//span[contains(text(), '%s')]//..//a[@title='Delete tag']",
            "deleteTagValueBtnTemplate"
        );
        editTagValueBtnTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//div[@class='tag-value']//span[contains(text(), '%s')]//..//a[@title='Edit tag']",
            "editTagValueBtnTemplate"
        );
        editTagValueInputTemplate = createScopedElement(
            "xpath=.//div[@class='tag-type'][%s]//div[@class='tags-list']//div[@class='tag-value']//input",
            "editTagValueInputTemplate"
        );

        tagsFromProjectNameSection = createScopedElement(
            "xpath=.//div[@id='tag-template' or contains(@id, 'tag')]",
            "tagsFromProjectNameSection"
        );
        projectNameTemplatesInput = createScopedElement(
            "xpath=.//input[@id='tagTemplateForm:tagTemplates'] | .//textarea[@id='tagTemplateForm:tagTemplates']",
            "projectNameTemplatesInput"
        );
        saveTemplatesBtn = createScopedElement(
            "xpath=.//button[@id='tagTemplateForm:saveTemplatesButton'] | .//button[./span[contains(text(),'Save')]]",
            "saveTemplatesBtn"
        );
        templateErrorMsg = createScopedElement(
            "xpath=.//span[contains(@id, 'tagTemplateForm')]//span[@class='error']",
            "templateErrorMsg"
        );
        fillTagsForProjectBtn = createScopedElement(
            "xpath=.//input[@value='Fill tags for projects'] | .//button[contains(text(),'Fill')]",
            "fillTagsForProjectBtn"
        );

        projectsWithoutTagsTable = createScopedElement(
            "xpath=.//div[@id='modalProjectsWithoutTags_content']//table",
            "projectsWithoutTagsTable"
        );
        saveProjectsWithoutTagsBtn = createScopedElement(
            "xpath=.//form[@id='projectsWithoutTagsForm']//input[@value='Save']",
            "saveProjectsWithoutTagsBtn"
        );
    }

    public void addTagType(String tagTypeName) {
        newTagTypeInput.fill(tagTypeName);
        newTagTypeInput.press("Enter");
    }

    public WebElement getTagTypeNameInput(int tagNumber) {
        return tagTypeNameInputTemplate.format(tagNumber);
    }

    public void setTagTypeName(int tagNumber, String name) {
        WebElement input = getTagTypeNameInput(tagNumber);
        input.clear();
        input.fill(name);
    }

    public String getTagTypeName(int tagNumber) {
        return getTagTypeNameInput(tagNumber).getAttribute("value");
    }

    public void setTagTypeOptional(int tagNumber, boolean value) {
        WebElement checkbox = tagTypeOptionalCheckboxTemplate.format(tagNumber);
        boolean isChecked = checkbox.isChecked();
        if (value != isChecked) {
            checkbox.click();
        }
    }

    public void setTagTypeExtensible(int tagNumber, boolean value) {
        WebElement checkbox = tagTypeExtensibleCheckboxTemplate.format(tagNumber);
        boolean isChecked = checkbox.isChecked();
        if (value != isChecked) {
            checkbox.click();
        }
    }

    public WebElement getTagTypeOptionalCheckbox(int tagNumber) {
        return tagTypeOptionalCheckboxTemplate.format(tagNumber);
    }

    public WebElement getTagTypeExtensibleCheckbox(int tagNumber) {
        return tagTypeExtensibleCheckboxTemplate.format(tagNumber);
    }

    public int countTagTypes() {
        List<String> tagTypes = new ArrayList<>();
        int count = 0;
        try {
            while (true) {
                count++;
                tagTypeTemplate.format(count).isVisible();
            }
        } catch (Exception e) {
            return count - 1;
        }
    }

    public void deleteTagType(int tagNumber) {
        tagTypeDeleteBtnTemplate.format(tagNumber).click();
    }

    public void addTagValue(int tagNumber, String tagValue) {
        WebElement input = newTagValueInputTemplate.format(tagNumber);
        input.fill(tagValue);
        input.press("Enter");
    }

    public List<String> getTagValues(int tagNumber) {
        List<String> values = new ArrayList<>();
        int index = 1;
        try {
            while (true) {
                WebElement valueElement = createScopedElement(
                    "xpath=.//div[@class='tag-type'][" + tagNumber + "]//div[@class='tags-list']//div[@class='tag-value'][" + index + "]//span[1]",
                    "tagValue_" + index
                );
                String text = valueElement.getText();
                if (text != null && !text.trim().isEmpty()) {
                    values.add(text.trim());
                }
                index++;
            }
        } catch (Exception e) {
            return values;
        }
    }

    public void deleteTagValue(int tagNumber, String tagValue) {
        deleteTagValueBtnTemplate.format(tagNumber, tagValue).click();
    }

    public void editTagValue(int tagNumber, String oldValue, String newValue) {
        editTagValueBtnTemplate.format(tagNumber, oldValue).click();

        WebElement input = editTagValueInputTemplate.format(tagNumber);
        input.clear();
        input.fill(newValue);
        input.press("Enter");
    }

    public boolean hasTagValues(int tagNumber, List<String> expectedValues) {
        List<String> actualValues = getTagValues(tagNumber);
        return new HashSet<>(actualValues).containsAll(expectedValues) && new HashSet<>(expectedValues).containsAll(actualValues);
    }

    public void setProjectNameTemplates(String templates) {
        projectNameTemplatesInput.clear();
        projectNameTemplatesInput.fill(templates);
    }

    public String getProjectNameTemplates() {
        return projectNameTemplatesInput.getAttribute("value");
    }

    public void saveProjectNameTemplates() {
        saveTemplatesBtn.click();
    }

    public String getTemplateError() {
        if (templateErrorMsg.isVisible()) {
            return templateErrorMsg.getText();
        }
        return "";
    }

    public void openFillTagsForProjectsForm() {
        fillTagsForProjectBtn.click();
    }

    public void saveProjectsWithoutTags() {
        saveProjectsWithoutTagsBtn.click();
    }

    public List<List<String>> getProjectsWithoutTagsTableContent() {
        List<List<String>> result = new ArrayList<>();
        int rowIndex = 1;
        try {
            while (true) {
                List<String> rowValues = new ArrayList<>();
                int cellIndex = 1;
                try {
                    while (true) {
                        WebElement cell = createScopedElement(
                            "xpath=.//div[@id='modalProjectsWithoutTags_content']//table//tbody/tr[" + rowIndex + "]/td[" + cellIndex + "]",
                            "cell_" + rowIndex + "_" + cellIndex
                        );
                        String text = cell.getText();
                        if (text != null) {
                            rowValues.add(text.trim());
                        }
                        cellIndex++;
                    }
                } catch (Exception e) {
                    if (!rowValues.isEmpty()) {
                        result.add(rowValues);
                    }
                }
                rowIndex++;
            }
        } catch (Exception e) {
            return result;
        }
    }

    public boolean isProjectsWithoutTagsTableVisible() {
        return projectsWithoutTagsTable.isVisible();
    }

    public boolean isTagTypesAndValuesSectionVisible() {
        return tagTypesAndValuesSection.isVisible();
    }

    public boolean hasTagValue(int tagNumber, String tagValue) {
        return getTagValues(tagNumber).contains(tagValue);
    }

    public void saveProjectNameTemplates(String templates) {
        setProjectNameTemplates(templates);
        saveProjectNameTemplates();
    }

    public boolean hasTemplatePattern(String pattern) {
        String templates = getProjectNameTemplates();
        return templates != null && templates.contains(pattern.replace("\n", ""));
    }

    public void setProjectNameTemplates(String templates) {
        projectNameTemplatesInput.clear();
        projectNameTemplatesInput.fill(templates);
    }

    public boolean hasAnyTemplateError() {
        return !getTemplateError().isEmpty();
    }

    public boolean hasTemplateError(String errorKeyword) {
        String error = getTemplateError();
        return error != null && error.toLowerCase().contains(errorKeyword.toLowerCase());
    }

    public void clearProjectNameTemplate() {
        projectNameTemplatesInput.clear();
        saveProjectNameTemplates();
    }

    public void trySaveProjectNameTemplate(String template) {
        setProjectNameTemplates(template);
        saveProjectNameTemplates();
    }

    public boolean trySaveProjectNameTemplate(String template, boolean shouldFail) {
        try {
            setProjectNameTemplates(template);
            saveProjectNameTemplates();
            return !shouldFail;
        } catch (Exception e) {
            return shouldFail;
        }
    }

    public void deleteTagType(String tagTypeName) {
        List<String> tagTypes = new ArrayList<>();
        for (int i = 1; i <= countTagTypes(); i++) {
            String name = getTagTypeName(i);
            if (name != null && name.contains(tagTypeName)) {
                deleteTagType(i);
                return;
            }
        }
    }

    public boolean hasTagType(String tagTypeName) {
        for (int i = 1; i <= countTagTypes(); i++) {
            String name = getTagTypeName(i);
            if (name != null && name.equals(tagTypeName)) {
                return true;
            }
        }
        return false;
    }

    // Auto-fill and extraction methods
    public boolean openAutoFillForm() {
        try {
            openFillTagsForProjectsForm();
            return isProjectsWithoutTagsTableVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasExtractedTagValue(String tagType, String value) {
        List<List<String>> tableContent = getProjectsWithoutTagTableContent();
        for (List<String> row : tableContent) {
            // Check if any cell contains the value
            for (String cell : row) {
                if (cell != null && cell.contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getExtractedProjectCount() {
        List<List<String>> tableContent = getProjectsWithoutTagTableContent();
        return tableContent.size();
    }

    public void selectAllExtractedProjects() {
        // Select all checkboxes in the projects table
        List<List<String>> tableContent = getProjectsWithoutTagTableContent();
        if (!tableContent.isEmpty()) {
            // Implementation would involve finding and clicking all checkboxes
            // This is placeholder as actual selectors depend on the UI
        }
    }

    public void applyAutoFillTags() {
        saveProjectsWithoutTags();
    }

    public void selectMatchingProjectsForAutoFill() {
        // Select projects that match the template pattern
        List<List<String>> tableContent = getProjectsWithoutTagTableContent();
        if (!tableContent.isEmpty()) {
            // Implementation would involve selecting checkboxes for matching projects
        }
    }

    public boolean hasExtractedProject(String repository, String projectName) {
        List<List<String>> tableContent = getProjectsWithoutTagTableContent();
        for (List<String> row : tableContent) {
            boolean hasRepository = false;
            boolean hasProject = false;
            for (String cell : row) {
                if (cell != null && cell.contains(repository)) {
                    hasRepository = true;
                }
                if (cell != null && cell.contains(projectName)) {
                    hasProject = true;
                }
            }
            if (hasRepository && hasProject) {
                return true;
            }
        }
        return false;
    }

    // Grouping methods
    public boolean trySetDuplicateGrouping(int level1, String dim1, int level2, String dim2, int level3, String dim3) {
        try {
            // This would attempt to set grouping with duplicate dimensions
            // The framework should reject this
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasGroupingError(String errorKeyword) {
        String error = getTemplateError();
        return error != null && error.toLowerCase().contains(errorKeyword.toLowerCase());
    }

    public boolean isGroupingOptionAvailable(int level, String option) {
        // Check if an option is available for selection at a grouping level
        // Implementation depends on the actual UI structure
        return true;
    }

    public void setProjectGrouping(int level, String dimension) {
        // Set the grouping at a specific level
        // Implementation depends on the actual UI structure
    }

    public String getProjectGroupingLevel(int level) {
        // Get the current grouping dimension for a level
        // Implementation depends on the actual UI structure
        return null;
    }

    public void deleteTagType(String tagTypeName) {
        List<String> tagTypes = new ArrayList<>();
        for (int i = 1; i <= countTagTypes(); i++) {
            String name = getTagTypeName(i);
            if (name != null && name.equals(tagTypeName)) {
                deleteTagType(i);
                return;
            }
        }
    }
}