package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class ProjectDeleteConfirmModalComponent extends BaseComponent {

    private WebElement commentInput;
    private WebElement acknowledgeCheckbox;
    private WebElement deleteBtn;
    private WebElement cancelBtn;

    public ProjectDeleteConfirmModalComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        String modalRoot = "//div[contains(@class,'ant-modal') and .//textarea[@id='comment']]";
        commentInput = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//textarea[@id='comment']", "projectDeleteCommentInput");
        acknowledgeCheckbox = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//input[@id='confirmed']", "projectDeleteAcknowledgeCheckbox");
        deleteBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[contains(@class,'ant-btn-dangerous')]", "projectDeleteConfirmBtn");
        cancelBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[normalize-space(.)='Cancel']", "projectDeleteCancelBtn");
    }

    public ProjectDeleteConfirmModalComponent waitForVisible() {
        commentInput.waitForVisible();
        return this;
    }

    public ProjectDeleteConfirmModalComponent enterDeletionComment(String comment) {
        commentInput.fill(comment);
        return this;
    }

    public ProjectDeleteConfirmModalComponent acknowledgePermanentDeletion() {
        acknowledgeCheckbox.check();
        return this;
    }

    public boolean isDeleteEnabled() {
        return deleteBtn.isEnabled();
    }

    public void clickDelete() {
        deleteBtn.click();
        deleteBtn.waitForHidden(5000);
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
