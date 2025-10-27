package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TableComponent;

public class WorkspaceComponent extends BaseComponent {

    private TableComponent workspaceProjectsTable;
    private TableComponent repositoryTable;
    private WebElement createBtn;
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
        workspaceProjectsTable = createScopedComponent(TableComponent.class, ".//table[@id='uploadPrjFromLocalTable']", "workspaceProjectsTable");
        repositoryTable = createScopedComponent(TableComponent.class, ".//table[@id='workPanelRepo']", "repositoryTable");
        createBtn = createScopedElement(".//input[@name='sbtWorkspaceBtn']", "createBtn");
        cancelBtn = createScopedElement(".//button[@value='Cancel']", "cancelBtn");
    }

    public void save() {
        createBtn.click();
    }

    public void cancel() {
        cancelBtn.click();
    }
}