package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class SaveProjectDialogComponent extends BaseComponent {

    private static final String MODAL_ROOT =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title') and contains(normalize-space(),'Save project')]]";

    private WebElement commentField;
    private WebElement submitBtn;
    private WebElement cancelBtn;

    public SaveProjectDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        commentField = new WebElement(DriverPool.getPage(),
                "[data-testid=save-project-comment]", "saveProjectComment");
        submitBtn = new WebElement(DriverPool.getPage(),
                "[data-testid=save-project-submit]", "saveProjectSubmit");
        cancelBtn = new WebElement(DriverPool.getPage(),
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

    public void submitThroughShade() {
        submitBtn.clickForce();
        submitBtn.waitForHidden(10000);
    }


    public void clickSubmit() {
        submitBtn.click();
    }

    public void waitForSubmitHidden() {
        submitBtn.waitForHidden(10000);
    }

    public void cancel() {
        cancelBtn.click();
    }
}
