package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

// React "Save project" dialog (build 032c60a664ce+), opened from a project row's Save action when the
// project has uncommitted local changes. Replaces the legacy SaveChangesComponent.
public class SaveProjectDialogComponent extends BaseComponent {

    private static final String MODAL_ROOT =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Save project')]]";

    private WebElement commentField;
    private WebElement submitBtn;
    private WebElement cancelBtn;

    public SaveProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        commentField = new WebElement(LocalDriverPool.getPage(),
                "[data-testid=save-project-comment]", "saveProjectComment");
        submitBtn = new WebElement(LocalDriverPool.getPage(),
                "[data-testid=save-project-submit]", "saveProjectSubmit");
        cancelBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + MODAL_ROOT + "//button[normalize-space(.)='Cancel']", "saveProjectCancel");
    }

    public SaveProjectDialogComponent waitForVisible() {
        commentField.waitForVisible();
        return this;
    }

    public SaveProjectDialogComponent setComment(String comment) {
        commentField.fill(comment);
        return this;
    }

    public void submit() {
        submitBtn.click();
        submitBtn.waitForHidden(10000);
    }

    public void cancel() {
        cancelBtn.click();
    }
}
