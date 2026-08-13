package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.api.ProjectsMethod;
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

public class TestProjectNameValidationUi extends BaseTest {

    private static final String TRAILING_DOT_NAME = "BadName.";
    private static final String COLON_NAME = "Bad:Name";
    private static final String SLASH_NAME = "Bad/Name";
    private static final String SPACED_NAME = "  SpacedName  ";
    private static final String TRIMMED_NAME = "SpacedName";

    @Test
    @TestCaseId("EPBDS-16402")
    @Description("Negative: the Create Project modal must reject a name ending with a dot and a name with a "
            + "forbidden ':' character using the specific server validation message, must not create anything, "
            + "and must trim leading/trailing spaces from an otherwise valid name.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectNameValidation() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String trailingDotError = submitTemplateProjectExpectingError(repositoryPage, TRAILING_DOT_NAME);
        assertThat(trailingDotError)
                .as("A name ending with a dot must be rejected with the specific validation message")
                .contains("end with space or dot");
        repositoryPage.getCreateNewProjectComponent().cancelCreation();
        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectPresent(TRAILING_DOT_NAME))
                .as("The rejected trailing-dot name must not create a project")
                .isFalse();
        assertThat(new ProjectsMethod().getProjects(TRAILING_DOT_NAME).asString())
                .as("The REST projects list must not hold the rejected trailing-dot name")
                .doesNotContain(TRAILING_DOT_NAME);

        String colonError = submitTemplateProjectExpectingError(repositoryPage, COLON_NAME);
        assertThat(colonError)
                .as("A name with ':' must be rejected with the specific forbidden-characters message")
                .contains("forbidden characters");
        repositoryPage.getCreateNewProjectComponent().cancelCreation();
        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectPresent(COLON_NAME))
                .as("The rejected forbidden-character name must not create a project")
                .isFalse();
        assertThat(new ProjectsMethod().getProjects(COLON_NAME).asString())
                .as("The REST projects list must not hold the rejected forbidden-character name")
                .doesNotContain(COLON_NAME);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, SPACED_NAME, "Sample Project");
        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectPresent(TRIMMED_NAME))
                .as("Leading and trailing spaces must be trimmed and the project created under the clean name")
                .isTrue();
    }

    @Test
    @TestCaseId("EPBDS-16439")
    @Description("EPBDS-16439 (open defect, expected to fail until fixed): a name containing '/' must get the same "
            + "specific forbidden-characters validation message as every other forbidden character, but the create "
            + "request dies on URL routing with a blank 400 and the modal shows the generic "
            + "'Something went wrong on API server!' instead.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSlashNameShowsSpecificValidationMessage() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String slashError = submitTemplateProjectExpectingError(repositoryPage, SLASH_NAME);
        assertThat(slashError)
                .as("A name with '/' must be rejected with the specific forbidden-characters message, "
                        + "not a generic API error (EPBDS-16439)")
                .contains("forbidden characters");
    }

    private String submitTemplateProjectExpectingError(RepositoryPage repositoryPage, String projectName) {
        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent createDialog = repositoryPage.getCreateNewProjectComponent();
        createDialog.createProjectFromTemplate("Sample Project", projectName, true);
        return createDialog.getError();
    }
}
