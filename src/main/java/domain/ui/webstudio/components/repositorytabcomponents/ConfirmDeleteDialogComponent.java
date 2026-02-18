package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

public class ConfirmDeleteDialogComponent extends BaseComponent {

    private WebElement deleteBtn;
    private WebElement cancelBtn;

    public ConfirmDeleteDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ConfirmDeleteDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        deleteBtn = createScopedElement("xpath=.//form[@id='deleteNodeForm']//input[@value='Delete']", "deleteBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='deleteNodeForm']//input[@value='Cancel']", "cancelBtn");
    }

    public void clickDelete() {
        deleteBtn.click();
        WaitUtil.sleep(500, "Waiting for delete action to process");
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
