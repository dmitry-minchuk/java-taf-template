package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class WorkspaceComponent extends CoreComponent {

    private WebElement workspacePanel;
    private WebElement workspacePathField;
    private WebElement browseBtn;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public WorkspaceComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public WorkspaceComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        workspacePanel = createScopedElement(".//div[contains(@class,'workspace-component')]", "workspacePanel");
        workspacePathField = createScopedElement(".//input[@placeholder='Workspace Path' or contains(@id,'workspacePath')]", "workspacePathField");
        browseBtn = createScopedElement(".//button[./span[text()='Browse'] or contains(@title,'Browse')]", "browseBtn");
        saveBtn = createScopedElement(".//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement(".//button[./span[text()='Cancel']]", "cancelBtn");
    }

    public void setWorkspacePath(String path) {
        workspacePathField.fill(path);
    }

    public String getWorkspacePath() {
        return workspacePathField.getAttribute("value");
    }

    public void clickBrowse() {
        browseBtn.click();
    }

    public void save() {
        saveBtn.click();
    }

    public void cancel() {
        cancelBtn.click();
    }
}