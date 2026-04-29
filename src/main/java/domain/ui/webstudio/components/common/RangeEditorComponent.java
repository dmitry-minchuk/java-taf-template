package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class RangeEditorComponent extends BaseComponent {

    private WebElement doneBtn;
    private WebElement discardChangesBtn;

    public RangeEditorComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RangeEditorComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        doneBtn = createScopedElement("xpath=.//input[@value='Done']", "Range Editor Done Button");
        discardChangesBtn = new WebElement(page,
                "xpath=//form[@id='discardChangesForm' and not(ancestor::div[@style='display: none;' or @style='visibility: hidden; display: none;'])]//input[@value='Discard changes']",
                "Discard Changes Button");
    }

    public boolean isOpen() {
        return doneBtn.isVisible();
    }

    public boolean isOpen(int timeoutInMillis) {
        return doneBtn.isVisible(timeoutInMillis);
    }

    public void clickDone() {
        doneBtn.click();
    }

    public void discardChangesIfPresent() {
        if (discardChangesBtn.isVisible(1000)) {
            discardChangesBtn.click();
        }
    }
}
