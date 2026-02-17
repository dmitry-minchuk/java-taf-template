package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

@Getter
public class ExportProjectDialogComponent extends BaseComponent {

    private WebElement revisionDropdown;
    private WebElement exportBtn;
    private WebElement cancelBtn;

    public ExportProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ExportProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionDropdown = createScopedElement("xpath=.//select[@id='exportProjectForm:projectVersionToExport']", "revisionDropdown");
        exportBtn = createScopedElement("xpath=.//input[@value='Export']", "exportBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void waitForDialogToAppear() {
        revisionDropdown.waitForVisible(5000);
    }

    public boolean isDialogVisible() {
        return revisionDropdown.isVisible(2000);
    }

    public List<String> getAllRevisions() {
        return revisionDropdown.getSelectVisibleTextValues();
    }

    public String getSelectedRevision() {
        return revisionDropdown.getLocator().inputValue();
    }

    public void selectRevision(String revision) {
        revisionDropdown.selectByVisibleText(revision);
    }

    public void clickExport() {
        exportBtn.click();
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
