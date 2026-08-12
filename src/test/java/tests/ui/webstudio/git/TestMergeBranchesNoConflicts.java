package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.api.UsersMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeBranchesNoConflicts extends BaseTest {

    private static final String PROJECT_NAME = "NoConflicts";
    private static final String BRANCH_NAME = "MyBranch";
    private static final String MASTER_BRANCH = "master";
    private static final String SPREADSHEET = "Spreadsheet";

    @Test
    @TestCaseId("IPBQA-29455")
    @Description("Git - Merge branches without conflicts between master and MyBranch. Verifies EPBDS-8488: the "
            + "merge commit is authored by the WebStudio user's display name, not a .gitconfig identity. "
            + "KNOWN-FAILING: the first save on MyBranch - after Module4 is deleted and Module6 uploaded - fails "
            + "server-side with ProjectException \"Object ... is not a tree\", so the Save dialog never closes."
            + " Known bug: EPBDS-16361.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesNoConflicts() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME,
                "TestMergeBranchesNoConflicts_NoConflicts.zip");

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.isBranchPresent(BRANCH_NAME)).as("MyBranch absent before create").isFalse();
        projectDetail.createBranch(BRANCH_NAME, true);
        assertThat(projectDetail.getCurrentBranch()).as("on MyBranch after create").isEqualTo(BRANCH_NAME);

        SyncUpdatesDialogComponent syncDialog = projectDetail.openMergeDialog(MASTER_BRANCH);
        assertThat(syncDialog.getHeader()).as("Sync dialog header").contains("Sync updates");
        assertThat(syncDialog.isReceiveEnabled()).as("Receive disabled initially").isFalse();
        assertThat(syncDialog.isSendEnabled()).as("Send disabled initially").isFalse();
        syncDialog.close();

        projectDetail.openFilesTab();
        projectDetail.deleteFile("Module4.xlsx");
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module6.xlsx"));
        repositoryPage.openProjectsList().saveProject(PROJECT_NAME, "MyBranch: Module4 -> Module6");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        EditorToolbarPanelComponent editorToolbar = editorPage.getEditorToolbarPanelComponent();
        editSpreadsheetCell(editorPage, "Module2", "MySpr2");
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().clickSave();

        editorToolbar.switchBranch(MASTER_BRANCH);
        editSpreadsheetCell(editorPage, "Module1", "MySpr1");
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().clickSave();

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getCurrentBranch()).as("on master").isEqualTo(MASTER_BRANCH);
        projectDetail.openFilesTab();
        projectDetail.deleteFile("Module3.xlsx");
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module5.xlsx"));
        repositoryPage.openProjectsList().saveProject(PROJECT_NAME, "master: Module3 -> Module5");

        repositoryPage.openProjectDetail(PROJECT_NAME).openMergeDialog(BRANCH_NAME).clickReceive();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx",
                "TestMergeBranchesNoConflicts_Module5.xlsx", "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "Module3.xlsx", "Module4.xlsx");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.reloadPage();
        assertSpreadsheetEdited(editorPage, "Module2", "MySpr2");
        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        assertSpreadsheetEdited(editorPage, "Module1", "MySpr1");

        editorToolbar.switchBranch(BRANCH_NAME);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getCurrentBranch()).as("on MyBranch").isEqualTo(BRANCH_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx", "Module3.xlsx",
                "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "TestMergeBranchesNoConflicts_Module5.xlsx", "Module4.xlsx");

        projectDetail.openMergeDialog(MASTER_BRANCH).clickReceive();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx",
                "TestMergeBranchesNoConflicts_Module5.xlsx", "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "Module3.xlsx", "Module4.xlsx");

        String mergeComment = "Merge branch '" + BRANCH_NAME + "'";
        List<String> revisionDescriptions = projectDetail.getRevisionDescriptions();
        List<String> revisionAuthors = projectDetail.getRevisionAuthors();
        assertThat(revisionDescriptions)
                .as("Revision history should include the merge commit and the project creation")
                .anyMatch(d -> d.contains(mergeComment))
                .anyMatch(d -> d.contains("Project " + PROJECT_NAME + " is created"));

        String adminDisplayName = new UsersMethod().getProfileDisplayName(UserService.getUser(User.ADMIN));
        int mergeIndex = IntStream.range(0, revisionDescriptions.size())
                .filter(i -> revisionDescriptions.get(i).contains(mergeComment))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No merge revision found in: " + revisionDescriptions));
        assertThat(revisionAuthors.get(mergeIndex))
                .as("EPBDS-8488: the merge commit author must be the WebStudio user's display name, "
                        + "not a .gitconfig identity")
                .isEqualTo(adminDisplayName);
        assertThat(revisionAuthors)
                .as("Every revision should carry a non-empty author")
                .allMatch(a -> !a.isEmpty());
    }

    private void editSpreadsheetCell(EditorPage editorPage, String module, String spreadsheet) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, module);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, spreadsheet);
        editorPage.getCenterTable().editCell(3, 1, "Step1*");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
    }

    private void assertSpreadsheetEdited(EditorPage editorPage, String module, String spreadsheet) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, module);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, spreadsheet);
        assertThat(editorPage.getCenterTable().getCellText(3, 1))
                .as("%s cell should carry the edited text", spreadsheet)
                .contains("Step1", "*");
    }

    private void assertPresent(ProjectDetailPage detail, String... files) {
        for (String file : files) {
            assertThat(detail.isFilePresent(file)).as("%s should be present", file).isTrue();
        }
    }

    private void assertAbsent(ProjectDetailPage detail, String... files) {
        for (String file : files) {
            assertThat(detail.isFilePresent(file)).as("%s should be absent", file).isFalse();
        }
    }
}
