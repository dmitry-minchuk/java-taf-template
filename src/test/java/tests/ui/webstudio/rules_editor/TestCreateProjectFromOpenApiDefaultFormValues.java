package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCreateProjectFromOpenApiDefaultFormValues extends BaseTest {

    private static final String JSON_FILE = "openapi.json";

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Verify default values in the Create Project from OpenAPI form: empty name fields, pre-filled path defaults")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiDefaultFormValues() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);

        assertThat(openApiComponent.getProjectName())
                .as("Project name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getDataModuleName())
                .as("Data module name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getDataModulePathDisplay())
                .as("Data module path should default to 'rules/Models.xlsx'")
                .isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName())
                .as("Rules module name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should default to 'rules/Algorithms.xlsx'")
                .isEqualTo("rules/Algorithms.xlsx");

        repositoryPage.getCreateNewProjectComponent().closeDialog();

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla");

        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be visible after file upload")
                .isTrue();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be visible after file upload")
                .isTrue();

        openApiComponent.clearFirstFile();
        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be disabled after clearing the file")
                .isFalse();
        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be absent after file is cleared")
                .isFalse();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be absent after file is cleared")
                .isFalse();

        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.clearAllFiles();
        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be disabled after clearing all files")
                .isFalse();
        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be absent after ClearAll")
                .isFalse();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be absent after ClearAll")
                .isFalse();

        repositoryPage.getCreateNewProjectComponent().closeDialog();
    }
}
