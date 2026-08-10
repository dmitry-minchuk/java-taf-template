package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import lombok.Getter;

@Getter
public class SaveChangesComponent extends BaseComponent {

    private WebElement commentField;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public SaveChangesComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public SaveChangesComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        commentField = createScopedElement("xpath=.//textarea[@id='saveForm:comment']", "commentField");
        saveBtn = createScopedElement("xpath=.//input[@value='Save']", "Save Button");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "Cancel Button");
    }

    /**
     * Presses Save. The dialog's own shade can still be on top of the button right after another popup closed
     * (removing a module, for one), so a click that the shade swallows is repeated on the button itself.
     */
    public void clickSave() {
        try {
            saveBtn.click();
        } catch (RuntimeException shadeInTheWay) {
            saveBtn.clickForce();
        }
        saveBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
    }
}
