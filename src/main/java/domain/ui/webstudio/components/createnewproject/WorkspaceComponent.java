package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TableComponent;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceComponent extends BaseComponent {

    private static final int COL_CHECKBOX = 1;
    private static final int COL_PROJECT_NAME = 2;

    private TableComponent workspaceProjectsTable;
    private WebElement selectAllCheckbox;
    private WebElement repositorySelect;
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
        workspaceProjectsTable = createScopedComponent(TableComponent.class, "xpath=.//table[@id='uploadPrjFromLocalTable']", "workspaceProjectsTable");
        selectAllCheckbox = createScopedElement("xpath=.//input[@id='uploadPrjFromLocalTable:selectAll']", "selectAllCheckbox");
        repositorySelect = createScopedElement("xpath=.//select[@id='repositoryLocal']", "repositorySelect");
        createBtn = createScopedElement("xpath=.//input[@name='sbtWorkspaceBtn']", "createBtn");
        cancelBtn = createScopedElement("xpath=.//button[@value='Cancel']", "cancelBtn");
    }

    public WorkspaceComponent selectAllProjects() {
        selectAllCheckbox.click();
        WaitUtil.sleep(300, "Wait for all projects to be selected");
        return this;
    }

    public List<String> getAllAvailableProjects() {
        List<String> projectNames = new ArrayList<>();
        WaitUtil.waitForListNotEmpty(() -> workspaceProjectsTable.getRows(), 3000, 250, "Waiting for workspace projects to load");

        for (int i = 1; i <= workspaceProjectsTable.getRows().size(); i++) {
            String projectName = workspaceProjectsTable.getCellText(i, COL_PROJECT_NAME);
            if (!projectName.isEmpty()) {
                projectNames.add(projectName);
            }
        }
        return projectNames;
    }

    public WorkspaceComponent selectProject(String projectName) {
        int rowIndex = getProjectRowByName(projectName);
        workspaceProjectsTable.getCell(rowIndex, COL_CHECKBOX).getLocator().locator("xpath=.//input[@type='checkbox']").check();
        return this;
    }

    public WorkspaceComponent selectRepository(String repositoryName) {
        repositorySelect.selectByVisibleText(repositoryName);
        WaitUtil.sleep(300, "Wait for repository to be selected");
        return this;
    }

    public void save() {
        createBtn.click();
    }

    public void cancel() {
        cancelBtn.click();
    }

    private int getProjectRowByName(String projectName) {
        for (int i = 1; i <= workspaceProjectsTable.getRows().size(); i++) {
            if (workspaceProjectsTable.getCellText(i, COL_PROJECT_NAME).equals(projectName)) {
                return i;
            }
        }
        throw new RuntimeException("No such project found: " + projectName);
    }
}