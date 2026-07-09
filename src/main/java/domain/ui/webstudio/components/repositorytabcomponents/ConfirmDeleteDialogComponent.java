package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

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
        String modalRoot = "//div[contains(@class,'ant-modal') and "
                + ".//button[contains(@class,'ant-btn-dangerous')] and not(.//textarea[@id='comment'])]";
        deleteBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[contains(@class,'ant-btn-dangerous')]", "fileDeleteConfirmBtn");
        cancelBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[contains(@class,'ant-btn-default')]", "fileDeleteCancelBtn");
    }

    public void clickDelete() {
        deleteBtn.waitForVisible();
        deleteBtn.click();
        deleteBtn.waitForHidden(5000);
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
