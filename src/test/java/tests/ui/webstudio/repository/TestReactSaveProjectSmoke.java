package tests.ui.webstudio.repository;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Validates the React save/commit primitive (RepositoryPage.saveProject + SaveProjectDialogComponent,
// build 032c60a664ce+): a project with an uploaded (uncommitted) module exposes a Save row action, and
// saving commits it so the Save action disappears.
public class TestReactSaveProjectSmoke extends BaseTest {

    private static final String UPLOAD_FILE = "TestFileAddDelete.rules.xls";

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void saveCommitsLocalChanges() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName);
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources(UPLOAD_FILE));

        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Save"))
                .as("a project with an uploaded uncommitted module should expose the Save action").isTrue();

        repositoryPage.saveProject(projectName, "commit uploaded module");

        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Save"))
                .as("after saving, the project has no local changes so the Save action disappears").isFalse();
    }
}
