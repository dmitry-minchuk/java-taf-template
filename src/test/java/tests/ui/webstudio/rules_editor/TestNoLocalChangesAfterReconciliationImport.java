package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.ImportOpenApiDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNoLocalChangesAfterReconciliationImport extends BaseTest {

    private static final String OPENAPI_FILE_1 = "openapi1.json";
    private static final String OPENAPI_FILE_2 = "openapi2.json";

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Step 3: Local Changes do NOT appear after Reconciliation mode import – 'No changes in history' for both modules")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoLocalChangesAfterReconciliationImport() {
        String projectName = "TestLocalChanges3_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_1, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_2);
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Reconciliation import should not create local changes history for Algorithms")
                .isEqualTo("No changes in history");

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Reconciliation import should not create local changes history for Models")
                .isEqualTo("No changes in history");
    }

    private void uploadFileToProject(RepositoryPage repositoryPage, String projectName,
                                     String sourceFileName, String targetFileName) {
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(sourceFileName))
                .setFileName(targetFileName)
                .clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
    }
}
