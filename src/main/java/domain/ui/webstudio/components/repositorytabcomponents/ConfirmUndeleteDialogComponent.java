package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class ConfirmUndeleteDialogComponent extends BaseComponent {

    private WebElement undeleteBtn;
    private WebElement cancelBtn;

    public ConfirmUndeleteDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ConfirmUndeleteDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        undeleteBtn = createScopedElement("xpath=.//form[@id='modalUndeleteProjectForm']//input[@value='Undelete']", "undeleteBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='modalUndeleteProjectForm']//input[@value='Cancel']", "cancelBtn");
    }

    public void clickUndelete() {
        undeleteBtn.click();
        WaitUtil.sleep(500, "Waiting for undelete action to process");
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
