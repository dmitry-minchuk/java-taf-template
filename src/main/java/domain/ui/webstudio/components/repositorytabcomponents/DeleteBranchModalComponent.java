package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class DeleteBranchModalComponent extends BaseComponent {

    private WebElement modalBody;
    private WebElement lastBranchWarning;
    private WebElement confirmBtn;
    private WebElement cancelBtn;

    public DeleteBranchModalComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        String modalRoot = "//div[contains(@class,'ant-modal-wrap') and not(contains(@style,'display: none'))]"
                + "//div[contains(@class,'ant-modal-container')][contains(normalize-space(.),'Are you sure you want to delete branch')]";
        modalBody = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot, "deleteBranchModalBody");
        lastBranchWarning = new WebElement(DriverPool.getPage(),
                "[data-testid=delete-branch-last-branch-warning]", "deleteBranchLastBranchWarning");
        confirmBtn = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot + "//div[contains(@class,'ant-modal-footer')]//button[contains(@class,'ant-btn-dangerous')]",
                "deleteBranchConfirmBtn");
        cancelBtn = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot + "//button[normalize-space(.)='Cancel']", "deleteBranchCancelBtn");
    }

    public DeleteBranchModalComponent waitForVisible() {
        confirmBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public String getBodyText() {
        return modalBody.getText().trim();
    }

    public boolean isLastBranchWarningShown() {
        return lastBranchWarning.isVisible(DEFAULT_TIMEOUT_MS / 5);
    }

    public String getConfirmButtonLabel() {
        return confirmBtn.getText().trim();
    }

    public void clickDelete() {
        confirmBtn.click();
        confirmBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
    }

    public void attemptDelete() {
        confirmBtn.click();
    }

    public void clickCancel() {
        cancelBtn.click();
        confirmBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
    }
}
