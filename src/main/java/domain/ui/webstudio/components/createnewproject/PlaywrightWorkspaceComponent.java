package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightWorkspaceComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement workspacePanel;
    private PlaywrightWebElement workspacePathField;
    private PlaywrightWebElement browseBtn;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightWorkspaceComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightWorkspaceComponent(PlaywrightWebElement rootLocator) {
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

    public boolean isWorkspacePanelVisible() {
        return workspacePanel.isVisible();
    }

    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
    }
}