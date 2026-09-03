package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
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
    @Description("Create project from OpenAPI JSON file and verify repository tree structure and Editor module properties;"
            + " copying a module keeps the modules the descriptor already declares (EPBDS-16227) and an uploaded"
            + " workbook does not rewrite an explicit rules.xml.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiJsonFile() {
        String projectName = "JsonOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProjectFromOpenApi(JSON_FILE, projectName);

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName).openFilesTab();
        assertThat(projectDetail.isFilePresent("Algorithms.xlsx"))
                .as("Algorithms.xlsx should be present in the project files").isTrue();
        assertThat(projectDetail.isFilePresent("Models.xlsx"))
                .as("Models.xlsx should be present in the project files").isTrue();
        assertThat(projectDetail.isFilePresent("openapi.json"))
                .as("openapi.json should be present in the project files").isTrue();
        assertThat(projectDetail.isFilePresent("rules.xml"))
                .as("rules.xml should be present in the project files").isTrue();
        assertThat(projectDetail.isFilePresent("rules-deploy.xml"))
                .as("rules-deploy.xml should be present in the project files").isTrue();
        repositoryPage.openProjectsList();

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
        editorPage.waitUntilAppIdle();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.reloadPage();
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = new EditorPage().getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

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
        editorPage.getSaveChangesComponent().clickSave();
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
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        assertThat(editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName))
                .as("Copying a module must add it next to the modules the descriptor already declared (EPBDS-16227)")
                .containsExactlyInAnyOrder("Algorithms2", "Algorithms_test", "Models_test");
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterCopy = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterCopy = ZipUtil.readFileFromZip(exportedZipAfterCopy, "rules.xml");
        assertThat(rulesXmlAfterCopy).as("rules.xml must declare the copied module and keep the renamed ones (EPBDS-16227)")
                .contains("<name>Algorithms2</name>")
                .contains("<name>Algorithms_test</name>")
                .contains("<name>Models_test</name>");

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage detailAfterUpload = repositoryPage.openProjectsList().openProjectDetail(projectName);
        detailAfterUpload.uploadFileInto(TestDataUtil.getFilePathFromResources("rules.xlsx"), "rules");
        assertThat(detailAfterUpload.isFilePresent("rules.xlsx"))
                .as("rules.xlsx should be present in the project files after upload").isTrue();
        repositoryPage.openProjectsList().saveProject(projectName, "Uploaded rules.xlsx");

        EditorPage editorPageAfterUpload = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName))
                .as("Uploading a workbook must keep every module the explicit rules.xml declares and declare none itself")
                .containsExactlyInAnyOrder("Algorithms2", "Algorithms_test", "Models_test");

        editorPageAfterUpload.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterUpload = editorPageAfterUpload.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterUpload = ZipUtil.readFileFromZip(exportedZipAfterUpload, "rules.xml");
        assertThat(rulesXmlAfterUpload).as("rules.xml must be left as the copy wrote it after the upload")
                .isEqualTo(rulesXmlAfterCopy);
        assertThat(ZipUtil.listFiles(exportedZipAfterUpload))
                .as("The uploaded workbook must be part of the saved project")
                .contains("rules/rules.xlsx");

        repositoryPage = editorPageAfterUpload.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProjectsList().copyProject(projectName, projectName + "-Copy");

        assertThat(repositoryPage.isProjectPresent(projectName + "-Copy"))
                .as("Copied project '" + projectName + "-Copy' should appear in the projects list").isTrue();
    }
}
