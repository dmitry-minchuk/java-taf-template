package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class CopyModuleDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CopyModuleDialogComponent.class);

    private WebElement dialogContainer;
    private WebElement moduleNameInput;
    private WebElement newFileNameInput;
    private WebElement copyButton;
    private WebElement cancelButton;

    public CopyModuleDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CopyModuleDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dialogContainer = createScopedElement("xpath=//div[@id='copyModulePopup_container']", "dialogContainer");
        moduleNameInput = createScopedElement("xpath=.//input[@id='copyModuleForm:moduleName']", "moduleNameInput");
        newFileNameInput = createScopedElement("xpath=.//input[@id='copyModuleForm:newFileName']", "newFileNameInput");
        copyButton = createScopedElement("xpath=.//input[@id='copyModuleForm:copyModuleBtn']", "copyButton");
        cancelButton = createScopedElement("xpath=.//input[@value='Cancel']", "cancelButton");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> dialogContainer.isVisible(), 5000, 100, "Waiting for Copy Module dialog to appear");
    }

    public boolean isDialogVisible() {
        return dialogContainer.isVisible(2000);
    }

    public CopyModuleDialogComponent setModuleName(String moduleName) {
        LOGGER.info("Setting module name: {}", moduleName);
        moduleNameInput.clear();
        moduleNameInput.fill(moduleName);
        WaitUtil.sleep(300, "Waiting after module name input");
        return this;
    }

    public String getModuleName() {
        return moduleNameInput.getAttribute("value");
    }

    public String getNewFileName() {
        return newFileNameInput.getAttribute("value");
    }

    public void clickCopy() {
        LOGGER.info("Clicking Copy button");
        copyButton.click();
        WaitUtil.sleep(500, "Waiting for copy operation to complete");
    }

    public void clickCancel() {
        LOGGER.info("Clicking Cancel button");
        cancelButton.click();
        WaitUtil.sleep(300, "Waiting for dialog to close");
    }
}
