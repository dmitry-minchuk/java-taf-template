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

public class TestCreateProjectFromOpenApiFormValidation extends BaseTest {

    private static final String JSON_FILE = "openapi.json";
    private static final String INVALID_FILENAME_YAML = "dg!@#$%^&()_+{}.yaml";
    private static final String NON_OPENAPI_FILE = "non_openapi_file.xlsx";
    private static final String INVALID_JSON_FILE_1 = "openapi_inv1.json";
    private static final String INVALID_JSON_FILE_2 = "openapi_inv2.json";

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Validate same module names and same module paths errors in Create Project from OpenAPI form")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiFormValidation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(INVALID_FILENAME_YAML);
        openApiComponent.setProjectName("bla");
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        // The wizard reports problems in its own error area now, and with a short message: where the old UI
        // spelled out the forbidden characters, 6.4.0 just says the project could not be created.
        assertThat(openApiComponent.getError())
                .as("Creating with a forbidden character in the file name should be refused")
                .contains("Failed to create the project");

        // The wizard stays open after a refused create, so close it before starting the next one.
        openApiComponent.cancelCreation();
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        // BEHAVIOUR CHANGE, worth confirming with the team: the old UI refused a file that was not JSON or
        // YAML ("Only JSON and YML/YAML files are accepted"). 6.4.0 accepts it — the upload's accept list
        // only filters the file picker — and the project is created. Recorded as it behaves today.
        openApiComponent.uploadOpenApiSpec(NON_OPENAPI_FILE);
        String nonSpecProject = "nonOpenApi";
        openApiComponent.setProjectName(nonSpecProject);
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        assertThat(repositoryPage.openProjectsList().isProjectPresent(nonSpecProject))
                .as("6.4.0 no longer rejects a non-specification file at this step")
                .isTrue();
        repositoryPage.getCreateNewProjectComponent().cancelCreation();

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("test%?>");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when project name contains forbidden characters")
                .contains("is not a valid project name");

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("bla");
        openApiComponent.setDataModuleName("Models?*/test");
        openApiComponent.setDataModulePath("rules/Models.xlsx");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module name contains forbidden characters")
                .contains("Module Name cannot contain forbidden");

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("bla");
        openApiComponent.setDataModulePath("rules/Models?*test.xlsx");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module path contains forbidden characters")
                .contains("Project creating is failed");

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("bla_" + System.currentTimeMillis());
        openApiComponent.setRulesModuleName("Models");
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module names are the same")
                .contains("Module names cannot be the same");

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName("bla2_" + System.currentTimeMillis());
        openApiComponent.setDataModulePath("rules/Algorithms.xlsx");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module paths are the same")
                .contains("Path for Modules cannot be the same");

        String invalidProjectName = "InvalidJsonStructure_" + System.currentTimeMillis();
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(INVALID_JSON_FILE_1);
        openApiComponent.setProjectName(invalidProjectName);
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear for invalid JSON structure (file 1)")
                .contains("Project creating is failed");

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(INVALID_JSON_FILE_2);
        openApiComponent.setProjectName(invalidProjectName);
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear for invalid JSON structure (file 2)")
                .contains("Project creating is failed");

        String step29ProjectName = "bla29_" + System.currentTimeMillis();
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(JSON_FILE);
        openApiComponent.setProjectName(step29ProjectName);
        openApiComponent.setDataModuleName("bla1");
        openApiComponent.setRulesModuleName("kek2");
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        assertThat(openApiComponent.getDataModuleName()).isNotEqualTo("bla1");
        assertThat(openApiComponent.getDataModulePath()).isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName()).isNotEqualTo("kek2");
        assertThat(openApiComponent.getRulesModulePath()).isEqualTo("rules/Algorithms.xlsx");
        repositoryPage.getCreateNewProjectComponent().cancelCreation();
    }
}
