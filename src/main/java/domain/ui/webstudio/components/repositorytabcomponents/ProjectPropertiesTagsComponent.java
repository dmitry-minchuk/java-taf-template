package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class ProjectPropertiesTagsComponent extends BaseComponent {

    private WebElement tagsSection;
    private WebElement tagTypeRowTemplate;
    private WebElement tagValueInputTemplate;
    private WebElement tagValuesDropdownTemplate;
    private WebElement tagValueOptionTemplate;
    private WebElement tagErrorElement;

    public ProjectPropertiesTagsComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ProjectPropertiesTagsComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tagsSection = createScopedElement(
            "xpath=.//h3[contains(text(), 'Tags')]/..",
            "tagsSection"
        );
        tagTypeRowTemplate = createScopedElement(
            "xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]",
            "tagTypeRowTemplate"
        );
        tagValueInputTemplate = createScopedElement(
            "xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]//..//span//input[contains(@id, 'tag-name')]",
            "tagValueInputTemplate"
        );
        tagValuesDropdownTemplate = createScopedElement(
            "xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span//li",
            "tagValuesDropdownTemplate"
        );
        tagValueOptionTemplate = createScopedElement(
            "xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span//li[@value='%s']",
            "tagValueOptionTemplate"
        );
        tagErrorElement = createScopedElement(
            "xpath=.//div[@id='nodeTabPanel']//span[@class='error']",
            "tagErrorElement"
        );
    }

    public void selectTagValue(String tagType, String tagValue) {
        tagValueInputTemplate.format(tagType).click();
        tagValueOptionTemplate.format(tagType, tagValue).click();
    }

    public String getSelectedTagValue(String tagType) {
        WebElement input = tagValueInputTemplate.format(tagType);
        return input.getAttribute("value");
    }

    public List<String> getAvailableTagValues(String tagType) {
        List<String> values = new ArrayList<>();

        tagValueInputTemplate.format(tagType).click();

        int index = 1;
        try {
            while (true) {
                WebElement option = createScopedElement(
                    "xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '" + tagType + "')]/..//span//li[" + index + "]",
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

    public String getTagError() {
        if (tagErrorElement.isVisible()) {
            return tagErrorElement.getText();
        }
        return "";
    }

    public boolean isTagsSectionPresent() {
        return tagsSection.isVisible();
    }

    public boolean hasTagType(String tagType) {
        try {
            return tagTypeRowTemplate.format(tagType).isVisible();
        } catch (Exception e) {
            return false;
        }
    }
}
