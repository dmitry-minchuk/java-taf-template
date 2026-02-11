package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class FileChangedWarningComponent extends BaseComponent {

    private WebElement okButton;

    public FileChangedWarningComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public FileChangedWarningComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        okButton = createScopedElement("xpath=.//input[@value='OK']", "okButton");
    }

    public void clickOk() {
        okButton.click();
        WaitUtil.sleep(500, "Wait after confirming file change");
    }

    public boolean isDialogVisible() {
        try {
            return okButton.isVisible(1000);
        } catch (Exception e) {
            return false;
        }
    }

    public void clickOkIfPresent() {
        if (WaitUtil.waitForCondition(this::isDialogVisible, 2000, 100, "Check if file changed warning is present")) {
            clickOk();
        }
    }
}
