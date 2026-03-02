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
import domain.ui.webstudio.components.editortabcomponents.OpenApiModuleSettingsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNoNewRecordWhenRegeneratingFromSameFile extends BaseTest {

    private static final String OPENAPI_FILE_2 = "openapi2.json";

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Step 4: No new local change record when regenerating from a file with the same content as the original")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoNewRecordWhenRegeneratingFromSameFile() {
        String projectName = "TestLocalChanges4_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_2, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, "openapi2-copy.json");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath("openapi2-copy.json");
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Reimporting same content should not create a new local change record")
                .isEqualTo("Local Changes (0)");
        assertThat(changesDialog.getNoChangesMessage())
                .as("No history entries should exist when content is unchanged")
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
