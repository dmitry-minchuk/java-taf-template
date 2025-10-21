package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TagsInProjectCreationComponent extends BaseComponent {

    private WebElement createProjectTagsForm;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    private WebElement tagDropdownTemplate;
    private WebElement defaultTagValueInputTemplate;
    private WebElement tagValuesDropdownTemplate;
    private WebElement tagValueOptionTemplate;
    private WebElement newTagValueInputTemplate;

    public TagsInProjectCreationComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TagsInProjectCreationComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        createProjectTagsForm = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']",
            "createProjectTagsForm"
        );
        saveBtn = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//input[@value='Save']",
            "saveBtn"
        );
        cancelBtn = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//input[@value='Cancel']",
            "cancelBtn"
        );

        tagDropdownTemplate = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//td//input[@value='%s']/..//input[contains(@class, 'select')]",
            "tagDropdownTemplate"
        );
        defaultTagValueInputTemplate = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//td//input[@value='%s']/..//input[contains(@id, 'tag-name')]",
            "defaultTagValueInputTemplate"
        );
        tagValuesDropdownTemplate = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//td//li",
            "tagValuesDropdownTemplate"
        );
        tagValueOptionTemplate = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//td//li[@value='%s']",
            "tagValueOptionTemplate"
        );
        newTagValueInputTemplate = createScopedElement(
            "xpath=.//form[@id='createProjectTagsForm']//input[@value='%s']/..//input[contains(@class, 'select')]",
            "newTagValueInputTemplate"
        );
    }

    public void selectTagValue(String tagType, String tagValue) {
        tagDropdownTemplate.format(tagType).click();
        tagValueOptionTemplate.format(tagValue).click();
    }

    public List<String> getAvailableTagValues(String tagType) {
        List<String> values = new ArrayList<>();

        WebElement dropdown = tagDropdownTemplate.format(tagType);
        if (!dropdown.isVisible()) {
            dropdown.click();
        }

        int index = 1;
        try {
            while (true) {
                WebElement option = createScopedElement(
                    "xpath=.//form[@id='createProjectTagsForm']//td//li[" + index + "]",
                    "tagOption_" + index
                );
                String text = option.getText();
                if (text != null && !text.trim().isEmpty()) {
                    values.add(text.trim());
                }
                index++;
            }
        } catch (Exception e) {
            return values;
        }
    }

    public void enterNewTagValue(String tagType, String newValue) {
        WebElement input = newTagValueInputTemplate.format(tagType);
        input.fill(newValue);
        input.press("Enter");
    }

    public String getDefaultTagValue(String tagType) {
        WebElement input = defaultTagValueInputTemplate.format(tagType);
        return input.getAttribute("value");
    }

    public String getSelectedTagValue(String tagType) {
        return getDefaultTagValue(tagType);
    }

    public void save() {
        saveBtn.click();
    }

    public void cancel() {
        cancelBtn.click();
    }

    public boolean isFormVisible() {
        return createProjectTagsForm.isVisible();
    }
}
