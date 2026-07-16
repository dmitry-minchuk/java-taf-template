package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
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

public class TestFileAddDelete extends BaseTest {

    @Test
    @TestCaseId("NTC")
    @Description("Upload and delete a file via the React project-detail Files tab, twice.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFileAddDelete() {
        String fileName = "TestFileAddDelete.rules.xls";
        String filePath = TestDataUtil.getFilePathFromResources(fileName);

        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName);
        for (int cycle = 1; cycle <= 2; cycle++) {
            projectDetail.uploadFile(filePath);
            assertThat(projectDetail.isFilePresent(fileName))
                    .as("Uploaded file should be present before deletion (cycle %s)", cycle)
                    .isTrue();
            projectDetail.deleteFile(fileName);
            assertThat(projectDetail.isFilePresent(fileName))
                    .as("File should not be present after deletion (cycle %s)", cycle)
                    .isFalse();
        }
    }
}
