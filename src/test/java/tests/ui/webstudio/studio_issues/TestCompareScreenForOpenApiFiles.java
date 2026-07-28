package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
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
        ProjectDetailPage projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        projectDetail.uploadFileAs(TestDataUtil.getFilePathFromResources(OPENAPI_FILE_1), OPENAPI_FILE_NAME);
        repositoryPage.openProjectsList().saveProject(projectName, "Uploaded " + OPENAPI_FILE_NAME);

        // Read current revision for later use
        String revision = repositoryPage.openProjectsList().openProjectDetail(projectName).getOverviewRevision();

        // Replace openapi.json with openapi-compare2.json (a different file name, which is warned about)
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        projectDetail.updateFile(OPENAPI_FILE_NAME, TestDataUtil.getFilePathFromResources(OPENAPI_FILE_2));
        repositoryPage.openProjectsList().saveProject(projectName, "Updated " + OPENAPI_FILE_NAME);

        // Open the previous revision (R1)
        repositoryPage.openProjectsList().openProjectDetail(projectName).openRevisionByPosition(2);

        // In Editor: select Bank Rating module, navigate to MaxLimit table, edit a cell
        EditorPage editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MaxLimit");

        // Click Edit - this will trigger an alert about editing an old revision
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        // Edit a cell in the old revision (row 3, column 1, value "100")
        editorPage.getCenterTable().editCell(3, 1, "100");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        // Back in Repository: select openapi.json, update to openapi-compare3.json
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProjectsList().openProjectDetail(projectName)
                .updateFile(OPENAPI_FILE_NAME, TestDataUtil.getFilePathFromResources(OPENAPI_FILE_3));
    }
}
