package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOpenApiReconciliationSpreadsheetErrors extends BaseTest {

    private static final String ZIP_OPEN_API_BBB = "openApiBBB.zip";

    @Test
    @TestCaseId("EPBDS-13215")
    @Description("OpenAPI reconciliation: Spreadsheet cell type errors - cell type mismatch with OpenAPI schema")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationSpreadsheetErrors() {
        String projectName = "TestOpenApiSpreadsheet_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, ZIP_OPEN_API_BBB);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithm");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .expandFolderInTree("MySpr")
                .selectItemInFolder("MySpr", "MySpr [region=NCSA]");

        List<String> ncsaErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(ncsaErrors)
                .as("MySpr [region=NCSA] should have cell type mismatch error for $Step5")
                .containsExactly("OpenAPI Reconciliation: Type of cell '$Step5' must be compatible with OpenAPI type 'integer(int32)' that incompatible with actual schema 'object'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Spreadsheet", "MySpr");

        List<String> mysprErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(mysprErrors)
                .as("MySpr should have cell type mismatch error for $Step6")
                .containsExactly("OpenAPI Reconciliation: Type of cell '$Step6' must be compatible with OpenAPI type 'number(double)' that incompatible with actual schema 'object'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Spreadsheet", "MySpr2d");

        List<String> myspr2dErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(myspr2dErrors)
                .as("MySpr2d should contain both spreadsheet cell type errors")
                .contains(
                        "OpenAPI Reconciliation: Type of cell '$Calc2 Hello$Step5' must be compatible with OpenAPI type 'number' that incompatible with actual type 'string'.",
                        "OpenAPI Reconciliation: Type of cell '$Calc1$Step3' must be compatible with OpenAPI type 'string' that incompatible with actual type 'number(double)'."
                );
        assertThat(myspr2dErrors).hasSize(2);
    }
}
