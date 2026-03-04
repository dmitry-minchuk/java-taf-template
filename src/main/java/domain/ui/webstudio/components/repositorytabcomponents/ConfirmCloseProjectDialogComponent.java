package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class ConfirmCloseProjectDialogComponent extends BaseComponent {

    private WebElement closeProjectBtn;
    private WebElement cancelBtn;

    public ConfirmCloseProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        closeProjectBtn = createScopedElement("xpath=.//form[@id='closeProjectForm']//input[@value='Close Project']", "closeProjectBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='closeProjectForm']//input[@value='Cancel']", "cancelBtn");
    }

    public void clickClose() {
        closeProjectBtn.click();
        WaitUtil.sleep(500, "Waiting after closing project");
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
