package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Dialog;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ResolveConflictsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompareScreenForOpenApiFiles extends BaseTest {

    private static final String OPENAPI_FILE_1 = "openapi-compare.json";
    private static final String OPENAPI_FILE_2 = "openapi-compare2.json";
    private static final String OPENAPI_FILE_3 = "openapi-compare3.json";
    private static final String OPENAPI_FILE_NAME = "openapi.json";

    @Test
    @TestCaseId("EPBDS-10548")
    @Description("On conflict resolution screen for OpenAPI file, Compare screen must show 'DESIGN/rules/{projectName}/openapi.json' as file path")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompareScreenForOpenApiFiles() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN,
                "StudioIssues.TestCompareScreenForOpenApiFiles.zip");

        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Upload openapi-compare.json as "openapi.json" to the project
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(OPENAPI_FILE_1))
                .setFileName(OPENAPI_FILE_NAME)
                .clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

        // Read current revision for later use
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        String revision = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab()
                .getRevision();

        // Select openapi.json and update to openapi-compare2.json
        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder(projectName, OPENAPI_FILE_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUpdateFileBtn();
        repositoryPage.getUpdateFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUpdateFileDialogComponent()
                .updateFile(TestDataUtil.getFilePathFromResources(OPENAPI_FILE_2))
                .clickUpdateButton();
        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

        // Open the previous revision (R1) from the Revisions tab
        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentTabSwitcherComponent().selectRevisionsTab().openRevision(2);
        repositoryPage.waitUntilSpinnerLoaded();

        // In Editor: select Bank Rating module, navigate to MaxLimit table, edit a cell
        EditorPage editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MaxLimit");

        // Click Edit - this will trigger an alert about editing an old revision
        LocalDriverPool.getPage().onDialog(Dialog::accept);
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        WaitUtil.sleep(1000, "Waiting for edit mode to activate after accepting alert");

        // Edit a cell in the old revision (row 3, column 1, value "100")
        editorPage.getCenterTable().editCell(3, 1, "100");

        // Back in Repository: select openapi.json, update to openapi-compare3.json
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder(projectName, OPENAPI_FILE_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUpdateFileBtn();
        repositoryPage.getUpdateFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUpdateFileDialogComponent()
                .updateFile(TestDataUtil.getFilePathFromResources(OPENAPI_FILE_3))
                .clickUpdateButton();

        // Save – this triggers a conflict (editing old revision + new openapi.json change)
        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

        // Click Compare link in the Resolve Conflicts dialog
        ResolveConflictsDialogComponent conflictsDialog = repositoryPage.getResolveConflictsDialogComponent();
        conflictsDialog.waitForDialogToAppear();
        conflictsDialog.clickCompareLink();
        WaitUtil.sleep(1000, "Waiting for Compare screen to load");

        // Verify the file path shown in the Compare screen
        String expectedPath = "DESIGN/rules/" + projectName + "/" + OPENAPI_FILE_NAME;
        String actualPath = LocalDriverPool.getPage()
                .locator("xpath=//span[@class='d2h-file-name']")
                .textContent()
                .trim();

        assertThat(actualPath)
                .as("Compare screen should show the correct file path for the OpenAPI file")
                .isEqualTo(expectedPath);
    }
}
