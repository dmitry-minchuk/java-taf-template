package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import helpers.utils.ZipUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCreateProjectFromOpenApiJsonFile extends BaseTest {

    private static final String JSON_FILE = "openapi.json";

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Create project from OpenAPI JSON file and verify repository tree structure and Editor module properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiJsonFile() {
        String projectName = "JsonOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProjectFromOpenApi(JSON_FILE, projectName);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .expandFolderInTree(projectName)
                .expandFolderInTree("rules");

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Algorithms.xlsx"))
                .as("Algorithms.xlsx should be present in repository tree").isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Models.xlsx"))
                .as("Models.xlsx should be present in repository tree").isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("openapi.json"))
                .as("openapi.json should be present in repository tree").isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("rules.xml"))
                .as("rules.xml should be present in repository tree").isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("rules-deploy.xml"))
                .as("rules-deploy.xml should be present in repository tree").isTrue();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File property should reflect uploaded file name").isEqualTo("openapi.json");
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Tables generation'").isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module property should be 'Algorithms'").isEqualTo("Algorithms");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module property should be 'Models'").isEqualTo("Models");

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Algorithms module should have Spreadsheet folder").isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Configuration"))
                .as("Algorithms module should have Configuration folder").isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Algorithms module should NOT have Datatype folder").isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Models module should have Datatype folder").isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Models module should NOT have Spreadsheet folder").isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Datatype");
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "JAXRSErrorResponse");
        TableComponent datatypeTable = editorPage.getCenterTable();
        datatypeTable.editCell(3, 1, "String[]");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();

        String deployXml = ZipUtil.readFileFromZip(exportedZip, "rules-deploy.xml");
        assertThat(deployXml).as("rules-deploy.xml should contain isProvideRuntimeContext=true")
                .contains("<isProvideRuntimeContext>true</isProvideRuntimeContext>");
        assertThat(deployXml).as("rules-deploy.xml should contain RESTFUL publisher")
                .contains("<publisher>RESTFUL</publisher>");
        assertThat(deployXml).as("rules-deploy.xml should contain annotationTemplateClassName")
                .contains("<annotationTemplateClassName>org.openl.generated.services.Service</annotationTemplateClassName>");

        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        assertThat(rulesXml).as("rules.xml should contain Algorithms module")
                .contains("<name>Algorithms</name>").contains("<rules-root path=\"rules/Algorithms.xlsx\"/>");
        assertThat(rulesXml).as("rules.xml should contain Models module")
                .contains("<name>Models</name>").contains("<rules-root path=\"rules/Models.xlsx\"/>");
        assertThat(rulesXml).as("rules.xml should contain OpenAPI configuration")
                .contains("<path>openapi.json</path>")
                .contains("<model-module-name>Models</model-module-name>")
                .contains("<algorithm-module-name>Algorithms</algorithm-module-name>")
                .contains("<mode>GENERATION</mode>");

        editorPage.getProjectDetailsComponent().openEditModuleDialog("Algorithms");
        editorPage.getAddModulePopupComponent().setModuleName("Algorithms_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Models");
        editorPage.getAddModulePopupComponent().setModuleName("Models_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterRename = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterRename = ZipUtil.readFileFromZip(exportedZipAfterRename, "rules.xml");
        assertThat(rulesXmlAfterRename).as("rules.xml should reflect renamed modules Algorithms_test and Models_test")
                .contains("<name>Algorithms_test</name>").contains("<name>Models_test</name>")
                .contains("<model-module-name>Models_test</model-module-name>")
                .contains("<algorithm-module-name>Algorithms_test</algorithm-module-name>");
        assertThat(rulesXmlAfterRename).as("rules.xml should not contain old module names after rename")
                .doesNotContain("<model-module-name>Models</model-module-name>")
                .doesNotContain("<algorithm-module-name>Algorithms</algorithm-module-name>");

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.openCopyModuleDialog();
        editorPage.getCopyModuleDialogComponent().setModuleName("Algorithms2");
        editorPage.getCopyModuleDialogComponent().clickCopy();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        assertThat(editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName))
                .as("Algorithms2 module should be present after copy").contains("Algorithms2");

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects").selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources("rules.xlsx")).clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        EditorPage editorPageAfterUpload = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        assertThat(editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName))
                .as("Module 'rules' should appear after uploading rules.xlsx").contains("rules");

        editorPageAfterUpload.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterUpload = editorPageAfterUpload.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterUpload = ZipUtil.readFileFromZip(exportedZipAfterUpload, "rules.xml");
        assertThat(rulesXmlAfterUpload).as("rules.xml should contain Algorithms2 module after copy")
                .contains("<name>Algorithms2</name>");
        assertThat(rulesXmlAfterUpload).as("rules.xml should contain rules module after upload")
                .contains("<name>rules</name>");

        repositoryPage = editorPageAfterUpload.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects").selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent().clickCopyButton();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent().selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .setSeparateProject(true).setNewProjectName(projectName + "-Copy").clickCopyButton();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree(projectName + "-Copy"))
                .as("Copied project '" + projectName + "-Copy' should be present in repository tree").isTrue();
    }
}
