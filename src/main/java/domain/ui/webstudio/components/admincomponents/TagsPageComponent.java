package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import java.util.List;

public class TagsPageComponent extends BaseComponent {

    private static final int COL_TAG_TYPE = 1;
    private static final int COL_EXTENSIBLE = 2;
    private static final int COL_NULLABLE = 3;
    private static final int COL_TAGS = 4;
    private static final int COL_ACTIONS = 5;

    private TableComponent tagsTable;

    private WebElement newTagTypeInput;
    private WebElement projectNameTemplatesTextarea;
    private WebElement saveTemplatesBtn;
    private WebElement fillTagsForProjectBtn;

    public TagsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TagsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tagsTable = createScopedComponent(TableComponent.class, "xpath=.//div[@class='ant-table-wrapper tag-table']//table", "tagsTable");

        newTagTypeInput = createScopedElement("xpath=.//input[@name='tag-type' and @placeholder='New Tag Type']", "newTagTypeInput");
        projectNameTemplatesTextarea = createScopedElement("xpath=.//textarea[@class='ant-input']", "projectNameTemplatesTextarea");
        saveTemplatesBtn = createScopedElement("xpath=.//button[.//span[text()='Save Templates']]", "saveTemplatesBtn");
        fillTagsForProjectBtn = createScopedElement("xpath=.//button[.//span[text()='Fill Tags for Project']]", "fillTagsForProjectBtn");
    }

    public TableComponent getTagsTable() {
        tagsTable.isVisible();
        return tagsTable;
    }

    public int getTagTypeRowByName(String tagTypeName) {
        for (int i = 1; i <= tagsTable.getRows().size(); i++)
            if (getTagTypeName(i).equals(tagTypeName))
                return i;
        throw new RuntimeException("No such tagTypeName found!");
    }

    public TagsPageComponent editTagTypeName(String oldTagTypeName, String newTagTypeName) {
        int row = getTagTypeRowByName(oldTagTypeName);
        tagsTable.getCell(row, COL_TAG_TYPE).getLocator().locator("xpath=.//div[@class='editable-cell-wrap']").click();
        tagsTable.getCell(row, COL_TAG_TYPE).getLocator().locator("xpath=.//div/input").clear();
        tagsTable.getCell(row, COL_TAG_TYPE).getLocator().locator("xpath=.//div/input").fill(newTagTypeName);
        return this;
    }

    public String getTagTypeName(int rowIndex) {
        return tagsTable.getCell(rowIndex, COL_TAG_TYPE).getText().trim();
    }

    public boolean isExtensibleChecked(int rowIndex) {
        return tagsTable.getCell(rowIndex, COL_EXTENSIBLE).getLocator().locator("xpath=.//input[@type='checkbox']").isChecked();
    }

    public TagsPageComponent setExtensible(int rowIndex, boolean checked) {
        if (isExtensibleChecked(rowIndex) != checked)
            tagsTable.getCell(rowIndex, COL_EXTENSIBLE).getLocator().locator("xpath=.//input[@type='checkbox']").click();
        return this;
    }

    public boolean isNullableChecked(int rowIndex) {
        return tagsTable.getCell(rowIndex, COL_NULLABLE).getLocator().locator("xpath=.//input[@type='checkbox']").isChecked();
    }

    public TagsPageComponent setNullable(int rowIndex, boolean checked) {
        if (isNullableChecked(rowIndex) != checked)
            tagsTable.getCell(rowIndex, COL_NULLABLE).getLocator().locator("xpath=.//input[@type='checkbox']").click();
        return this;
    }

    public TagsPageComponent addTag(String tagTypeName, String tagToAdd) {
        int row = getTagTypeRowByName(tagTypeName);
        tagsTable.getCell(row, COL_TAGS).getLocator().locator("xpath=.//span[@class='ant-tag' and contains(@style, 'dashed')]").click();
        tagsTable.getCell(row, COL_TAGS).getLocator().locator("xpath=.//input").fill(tagToAdd);
        return this;
    }

    public TagsPageComponent removeTag(String tagTypeName, String tagText) {
        int row = getTagTypeRowByName(tagTypeName);
        tagsTable.getCell(row, COL_TAGS).getLocator()
                .locator(String.format("xpath=.//span[@class='ant-tag' and .//span[text()='%s']]", tagText))
                .locator("xpath=./span[@class='anticon anticon-close']").click();
        return this;
    }

    public List<String> getAllTagsForTagType(String tagTypeName) {
        int row = getTagTypeRowByName(tagTypeName);
        return tagsTable.getCell(row, COL_TAGS).getLocator()
                .locator("xpath=.//div/span[not(contains(@style,'dashed'))]/span[1]")
                .allTextContents();
    }

    public TagsPageComponent deleteTagType(String tagTypeName) {
        int row = getTagTypeRowByName(tagTypeName);
        tagsTable.getCell(row, COL_ACTIONS).getLocator().locator("xpath=.//span[@role='img' and @aria-label='delete']").click();
        return this;
    }

    public TagsPageComponent addNewTagType(String tagTypeName) {
        newTagTypeInput.fill(tagTypeName);
        newTagTypeInput.press("Enter");
        return this;
    }

    public TagsPageComponent setProjectNameTemplates(String templates) {
        projectNameTemplatesTextarea.fill(templates);
        return this;
    }

    public String getProjectNameTemplates() {
        return projectNameTemplatesTextarea.getAttribute("value");
    }

    public TagsPageComponent saveTemplates() {
        saveTemplatesBtn.click();
        return this;
    }

    public TagsPageComponent fillTagsForProject() {
        fillTagsForProjectBtn.click();
        return this;
    }
}
