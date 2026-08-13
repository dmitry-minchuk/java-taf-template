package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
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

public class TestFilesTabStaleSelectionUi extends BaseTest {

    private static final String UPLOAD_FILE = "TestFileAddDelete.rules.xls";
    private static final String MISSING_TEXT_FILE = "NeverExisted.xml";

    @Test
    @TestCaseId("EPBDS-16437")
    @Description("EPBDS-16437: deleting the file the Files tab has selected must drop that selection - the file "
            + "pane must not be left with the 'The resource is not found.' banner.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSelectionOfDeletedFileIsDropped() {
        ProjectDetailPage detail = openProjectWithUploadedFile();

        detail.selectFile(UPLOAD_FILE);
        assertThat(DriverPool.getPage().url())
                .as("Precondition: selecting a file must put that file into the URL")
                .contains(UPLOAD_FILE);

        detail.deleteFile(UPLOAD_FILE);
        assertThat(detail.isFilePresent(UPLOAD_FILE))
                .as("Precondition: the selected file must be gone after the deletion")
                .isFalse();
        assertThat(detail.isResourceNotFoundShown())
                .as("Deleting the selected file must drop the selection, not leave the not-found banner")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-16441")
    @Description("EPBDS-16441 (open defect, expected to fail until fixed): a deep link naming a file the tree "
            + "does not hold must have its stale selection dropped, but the Files pane opens that phantom file "
            + "and shows the 'The resource is not found.' banner instead.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testStaleDeepLinkSelectionIsDropped() {
        ProjectDetailPage detail = openProjectWithUploadedFile();

        detail.selectFile(UPLOAD_FILE);
        String urlWithSelection = DriverPool.getPage().url();
        assertThat(urlWithSelection)
                .as("Precondition: selecting a file must put that file into the URL")
                .contains(UPLOAD_FILE);

        DriverPool.getPage().navigate(urlWithSelection.replace(UPLOAD_FILE, MISSING_TEXT_FILE));
        detail = new ProjectDetailPage();
        assertThat(detail.isFilesTabOpen())
                .as("A deep link naming a missing file must still open the Files tab")
                .isTrue();
        assertThat(detail.isResourceNotFoundShown())
                .as("The stale deep-link selection must be dropped, not shown as the not-found banner "
                        + "(EPBDS-16441)")
                .isFalse();
    }

    private ProjectDetailPage openProjectWithUploadedFile() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage detail = repositoryPage.openProjectDetail(projectName);
        detail.uploadFile(TestDataUtil.getFilePathFromResources(UPLOAD_FILE));
        return detail;
    }
}
