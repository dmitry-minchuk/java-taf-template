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
        moduleNameInput = createScopedElement("xpath=.//input[@id='copyModuleForm:moduleName']", "moduleNameInput");
        newFileNameInput = createScopedElement("xpath=.//input[@id='copyModuleForm:newFileName']", "newFileNameInput");
        copyButton = createScopedElement("xpath=.//input[@id='copyModuleForm:copyModuleBtn']", "copyButton");
        cancelButton = createScopedElement("xpath=.//input[@value='Cancel']", "cancelButton");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> moduleNameInput.isVisible(), 1000, 100, "Waiting for Copy Module dialog to appear");
    }

    public CopyModuleDialogComponent setModuleName(String moduleName) {
        moduleNameInput.clear();
        WaitUtil.sleep(500, "Waiting before module name input");
        moduleNameInput.fillSequentially(moduleName);
        return this;
    }

    public String getModuleName() {
        return moduleNameInput.getAttribute("value");
    }

    public String getNewFileName() {
        return newFileNameInput.getAttribute("value");
    }

    public void clickCopy() {
        WaitUtil.waitForCondition(() -> {
            try {
                copyButton.click();
            } catch (Exception e) {
                LOGGER.info("Failed to click Copy button, retrying...");
            }
            return !copyButton.isVisible();
        }, 1000, 200, "Clicking Copy button in Copy Module dialog");
    }

    public void clickCancel() {
        cancelButton.click();
    }
}
