package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectIdUrlSafeEncodingUi extends BaseTest {

    private static final String PROJECT = "Тест-оПА";
    private static final String MODULE = "Main";

    @Test
    @TestCaseId("EPBDS-16402")
    @Description("EPBDS-16402: the standard Base64 of this project's id contains both '+' and '/', so a regression "
            + "to non-URL-safe ids breaks every screen with HTTP 400. The id in the URL must stay URL-safe through "
            + "create, project detail, Files, Revisions, the editor bridge, a deep link and delete.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUrlSafeIdSurvivesFullProjectLifecycle() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT, "Sample Project");

        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(PROJECT);
        String detailUrl = DriverPool.getPage().url();
        String idSegment = detailUrl.substring(detailUrl.lastIndexOf('/') + 1);
        assertThat(idSegment)
                .as("The project id in the URL must use only the URL-safe Base64 alphabet")
                .matches("[A-Za-z0-9_=-]+");
        String paddedId = idSegment + "=".repeat((4 - idSegment.length() % 4) % 4);
        assertThat(new String(Base64.getUrlDecoder().decode(paddedId), StandardCharsets.UTF_8))
                .as("The URL segment must decode as the URL-safe Base64 of the real project id")
                .contains(PROJECT);

        detail.openFilesTab();
        assertThat(detail.isFilePresent("Main.xlsx"))
                .as("The Files tab must list the template module when the project id needs URL-safe encoding")
                .isTrue();

        detail.openHistoryTab();
        assertThat(detail.getRevisionsCount())
                .as("The Revisions tab must load the history when the project id needs URL-safe encoding")
                .isGreaterThanOrEqualTo(1);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, MODULE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().waitForTreeFoldersToLoad().getAllEndNodesNames())
                .as("The editor must open the module of the project whose id needs URL-safe encoding")
                .isNotEmpty();

        DriverPool.getPage().navigate(detailUrl);
        detail = new ProjectDetailPage();
        assertThat(detail.getStatus())
                .as("A deep link holding the URL-safe id must open the project detail, not HTTP 400")
                .isNotBlank();

        repositoryPage.openProjectsList();
        repositoryPage.closeProject(PROJECT);
        repositoryPage.deleteProject(PROJECT)
                .enterDeletionComment("Removed by the URL-safe id regression test")
                .acknowledgePermanentDeletion()
                .clickDelete();
        repositoryPage.waitUntilSpinnerLoaded();
        assertThat(repositoryPage.isProjectPresent(PROJECT))
                .as("Delete must work for the project whose id needs URL-safe encoding")
                .isFalse();
    }
}
