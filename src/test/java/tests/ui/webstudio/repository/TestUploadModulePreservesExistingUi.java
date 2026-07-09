package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUploadModulePreservesExistingUi extends BaseTest {

    private static final String PROJECTS_FOLDER = "Projects";
    private static final String UPLOAD_MODULE = "TestMergeBranchesNoConflicts_Module5.xlsx";
    private static final String EXISTING_MODULE = "Main";

    @Test
    @TestCaseId("EPBDS-16227")
    @Description("Regression guard for EPBDS-16227 (existing modules disappearing when a new Excel module is uploaded). This generic single-module scenario keeps the existing module on both 6.3.1-756c03 and 6.3.1-1185c961 and does NOT reproduce the reported defect, which needs a specific multi-module project. Kept as a green guard for the upload-preserves-modules invariant.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUploadingExcelKeepsExistingModules() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECTS_FOLDER)
                .selectItemInFolder(PROJECTS_FOLDER, projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(UPLOAD_MODULE))
                .clickUploadButton();
        repositoryPage.waitUntilSpinnerLoaded();

        EditorPage editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        assertThat(editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName))
                .as("Existing module '%s' must remain in the module list after uploading another Excel module", EXISTING_MODULE)
                .contains(EXISTING_MODULE);
    }
}
