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
        CreateNewProjectComponent openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);

        assertThat(openApiComponent.getProjectName())
                .as("Project name should be empty by default")
                .isEmpty();
        // The React wizard offers module names and paths already filled in, instead of leaving the names empty.
        assertThat(openApiComponent.getDataModuleName())
                .as("Data module name should default to 'Models'")
                .isEqualTo("Models");
        assertThat(openApiComponent.getDataModulePath())
                .as("Data module path should default to 'rules/Models.xlsx'")
                .isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName())
                .as("Rules module name should default to 'Algorithms'")
                .isEqualTo("Algorithms");
        assertThat(openApiComponent.getRulesModulePath())
                .as("Rules module path should default to 'rules/Algorithms.xlsx'")
                .isEqualTo("rules/Algorithms.xlsx");

        repositoryPage.getCreateNewProjectComponent().cancelCreation();

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("bla");

        assertThat(openApiComponent.isOpenApiFileUploaded())
                .as("Clear button should be visible after file upload")
                .isTrue();

        openApiComponent.clearOpenApiFile();
        assertThat(openApiComponent.isOpenApiFileUploaded())
                .as("The specification should be gone after clearing it")
                .isFalse();
        // Create stays clickable; pressing it without a specification is what reports the problem.
        openApiComponent.clickCreate();
        assertThat(openApiComponent.getError())
                .as("Creating without a specification should ask for the file")
                .contains("Select an OpenAPI file");

        // Re-uploading and clearing again leaves the step in the same state.
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        assertThat(openApiComponent.isOpenApiFileUploaded())
                .as("The uploaded specification should be listed").isTrue();
        openApiComponent.clearOpenApiFile();
        assertThat(openApiComponent.isOpenApiFileUploaded())
                .as("The specification should be gone after clearing it").isFalse();

        repositoryPage.getCreateNewProjectComponent().cancelCreation();
    }
}
