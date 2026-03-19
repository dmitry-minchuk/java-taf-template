package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ImportOpenApiDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGenerateOpenApiDefaultDate extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10789")
    @Description("After clicking 'Create or Update Schema' in Import OpenAPI dialog, openapi.json appears in the repository tree")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGenerateOpenApiDefaultDate() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN,
                "StudioIssues.TestGenerateOpenApiDefaultDate.zip");

        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Open Import OpenAPI dialog and click 'Create or Update Schema' button
        // This generates openapi.json from the rules and places it in the repository
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.clickCreateOrUpdateSchema();
        editorPage.waitUntilSpinnerLoaded();

        // Navigate to Repository tab and verify openapi.json appears in the project tree
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects");
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName);
        repositoryPage.waitUntilSpinnerLoaded();

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("openapi.json"))
                .as("openapi.json should appear in the repository tree after generating the OpenAPI schema")
                .isTrue();
    }
}
