package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOpenApiReconciliationDependentProject extends BaseTest {

    private static final String ZIP_ARR_VALIDATION_DATATYPE = "ArrValidation_Datatype.zip";
    private static final String ZIP_ARR_VALIDATION_123 = "ArrValidation123.zip";

    @Test
    @TestCaseId("EPBDS-13215")
    @Description("OpenAPI reconciliation: Dependent project - errors propagate from dependency to dependent project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationDependentProject() {
        String projectName2 = "TestOpenApiArrDT_" + System.currentTimeMillis();
        String projectName3 = "TestOpenApiArr123_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName2, ZIP_ARR_VALIDATION_DATATYPE);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName3, ZIP_ARR_VALIDATION_123);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName3);

        editorPage.openManageDependenciesDialog().addDependency(projectName2, true);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName3, "ArrValidation");

        assertThat(editorPage.getProblemsPanelComponent().isErrorPresent("OpenAPI Reconciliation: Unexpected field 'someField' is found in type 'MyDatatype'."))
                .as("Error about unexpected field 'someField' should be present in Problems Panel")
                .isTrue();
        assertThat(editorPage.getProblemsPanelComponent().isErrorPresent("OpenAPI Reconciliation: Unexpected method 'myRule2' is found for path '/myRule2'."))
                .as("Error about unexpected method 'myRule2' should be present in Problems Panel")
                .isTrue();
        assertThat(editorPage.getProblemsPanelComponent().getErrorsCount())
                .as("Problems Panel should contain exactly 2 errors")
                .isEqualTo(2);
    }
}
