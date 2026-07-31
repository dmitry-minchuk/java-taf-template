package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;

public class MultiselectArrayEditorComponent extends BaseComponent {

    private WebElement valueCheckboxTemplate;
    private WebElement valueRowTemplate;
    // Short enough that a button lost to a re-render is retried rather than waited out.
    private static final int ACTION_BUTTON_CLICK_TIMEOUT_MS = DEFAULT_TIMEOUT_MS / 2;

    private WebElement actionButtonTemplate;
    private List<WebElement> allValueLabels;

    public MultiselectArrayEditorComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public MultiselectArrayEditorComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        valueCheckboxTemplate = createScopedElement("xpath=.//div[text()='%s']/input", "Multiselect value checkbox");
        valueRowTemplate = createScopedElement("xpath=.//div[text()='%s']", "Multiselect value row");
        actionButtonTemplate = new WebElement(page,
                "xpath=//div[@class='multiselect_buttons']/input[@value='%s']",
                "Multiselect action button");
        allValueLabels = createScopedElementList("xpath=./div[input[@type='checkbox']]", "All multiselect value labels");
    }

    public boolean isOpen() {
        return actionButtonTemplate.format("Done").isVisible(2000);
    }

    public boolean isValueChecked(String value) {
        return valueCheckboxTemplate.format(value).isChecked();
    }

    public void verifyChosenValues(List<String> values) {
        for (String value : values) {
            if (!isValueChecked(value)) {
                throw new AssertionError(String.format(
                        "Multiselect value '%s' should be checked but is not", value));
            }
        }
    }

    public void verifyNonChosenValues(String... values) {
        for (String value : values) {
            if (isValueChecked(value)) {
                throw new AssertionError(String.format(
                        "Multiselect value '%s' should NOT be checked but is", value));
            }
        }
    }

    public void selectValues(String... values) {
        for (String value : values) {
            WebElement checkbox = valueCheckboxTemplate.format(value);
            if (!checkbox.isChecked()) {
                checkbox.check();
            }
        }
    }

    public void deselectValues(String... values) {
        for (String value : values) {
            WebElement checkbox = valueCheckboxTemplate.format(value);
            if (checkbox.isChecked()) {
                checkbox.uncheck();
            }
        }
    }

    /**
     * Presses one of the editor's buttons (Done / Cancel / ...). The editor is re-rendered whenever a value is
     * ticked, so the button found a moment ago can be gone by the time it is pressed - the press is retried on
     * the current one instead of waiting the whole timeout out on a detached node.
     */
    public void clickActionButton(String buttonName) {
        WaitUtil.retryOnException(() -> {
            actionButtonTemplate.format(buttonName).click(ACTION_BUTTON_CLICK_TIMEOUT_MS);
            return null;
        }, DEFAULT_TIMEOUT_MS * 2, 500, "Pressing the multiselect button " + buttonName);
    }

    public List<String> getAllValues() {
        return allValueLabels.stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }
}
