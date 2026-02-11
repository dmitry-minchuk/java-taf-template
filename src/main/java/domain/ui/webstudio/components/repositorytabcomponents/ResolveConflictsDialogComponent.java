package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class ResolveConflictsDialogComponent extends BaseComponent {

    private WebElement useYoursRadio;
    private WebElement useTheirsRadio;
    private WebElement useBaseRadio;
    private WebElement uploadMergedRadio;
    private WebElement saveButton;
    private WebElement cancelButton;

    public ResolveConflictsDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ResolveConflictsDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        useYoursRadio = createScopedElement("xpath=.//input[@type='radio' and @value='OURS']", "useYoursRadio");
        useTheirsRadio = createScopedElement("xpath=.//input[@type='radio' and @value='THEIRS']", "useTheirsRadio");
        useBaseRadio = createScopedElement("xpath=.//input[@type='radio' and @value='BASE']", "useBaseRadio");
        uploadMergedRadio = createScopedElement("xpath=.//input[@type='radio' and @value='CUSTOM']", "uploadMergedRadio");
        saveButton = createScopedElement("xpath=.//button[contains(@class, 'ant-btn-primary') and contains(., 'Save and Resolve')]", "saveButton");
        cancelButton = createScopedElement("xpath=.//button[contains(@class, 'ant-btn-default') and contains(., 'Cancel')]", "cancelButton");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> useYoursRadio.isVisible(), 10000, 250, "Waiting for Resolve Conflicts dialog to appear");
    }

    public boolean isDialogVisible() {
        try {
            return useYoursRadio.isVisible(2000);
        } catch (Exception e) {
            return false;
        }
    }

    public void resolveConflictUseYours() {
        WaitUtil.waitForCondition(() -> useYoursRadio.isVisible(), 5000, 100, "Waiting for Use Yours radio button");
        useYoursRadio.click();
        WaitUtil.sleep(500, "Wait for radio button selection");
        clickSave();
    }

    public void resolveConflictUseTheirs() {
        WaitUtil.waitForCondition(() -> useTheirsRadio.isVisible(), 5000, 100, "Waiting for Use Theirs radio button");
        useTheirsRadio.click();
        WaitUtil.sleep(500, "Wait for radio button selection");
        clickSave();
    }

    public void resolveConflictUseBase() {
        WaitUtil.waitForCondition(() -> useBaseRadio.isVisible(), 5000, 100, "Waiting for Use Base radio button");
        useBaseRadio.click();
        WaitUtil.sleep(500, "Wait for radio button selection");
        clickSave();
    }

    public void clickSave() {
        WaitUtil.waitForCondition(() -> saveButton.isVisible() && saveButton.isEnabled(), 5000, 100, "Waiting for Save button");
        saveButton.click();
        WaitUtil.sleep(2000, "Wait for conflict resolution");
    }

    public void clickCancel() {
        cancelButton.click();
        WaitUtil.sleep(1000, "Wait for dialog to close");
    }
}
