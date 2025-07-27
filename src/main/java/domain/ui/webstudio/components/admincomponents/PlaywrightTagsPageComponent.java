package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightTagsPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement addTagBtn;
    private PlaywrightWebElement tagNameField;
    private PlaywrightWebElement tagDescriptionField;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;
    private PlaywrightWebElement tagsTable;

    public PlaywrightTagsPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTagsPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        addTagBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Add Tag'] or ./span[contains(text(),'Add')]]", "Add Tag Button");
        tagNameField = new PlaywrightWebElement(page, ".//input[@placeholder='Tag Name' or @id='tagName']", "Tag Name Field");
        tagDescriptionField = new PlaywrightWebElement(page, ".//input[@placeholder='Description' or @id='tagDescription'] | .//textarea[@placeholder='Description']", "Tag Description Field");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
        tagsTable = new PlaywrightWebElement(page, ".//table//tbody[@class='ant-table-tbody']", "Tags Table");
    }

    public void clickAddTag() {
        addTagBtn.click();
    }

    public void setTagName(String name) {
        tagNameField.fill(name);
    }

    public String getTagName() {
        return tagNameField.getAttribute("value");
    }

    public void setTagDescription(String description) {
        tagDescriptionField.fill(description);
    }

    public String getTagDescription() {
        return tagDescriptionField.getAttribute("value");
    }

    public void saveTag() {
        saveBtn.click();
    }

    public void cancelTag() {
        cancelBtn.click();
    }

    public void addNewTag(String name, String description) {
        clickAddTag();
        setTagName(name);
        setTagDescription(description);
        saveTag();
    }

    public boolean isTagsTableVisible() {
        return tagsTable.isVisible();
    }

    public boolean isAddTagButtonVisible() {
        return addTagBtn.isVisible();
    }
}