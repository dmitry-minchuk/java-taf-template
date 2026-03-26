package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestConstructorNotFoundInDependentTable extends BaseTest {

    @Test
    @TestCaseId("EPBDS-12848")
    @Description("EPBDS-12848: 'Constructor not found' error must not appear in dependent table after modifying main table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoConstructorNotFoundErrorInDependentTable() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create all 3 projects from attachments
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, "DoliMy", "EPBDS-12848_DoliMy.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, "TheProejct", "EPBDS-12848_TheProejct.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, "SomeTransProject", "EPBDS-12848_SomeTransProject.zip");

        // Step 1: Open DoliMy → module DoliMy → table mySpr3 — should have no errors
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule("DoliMy", "DoliMy");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr3");
        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("DoliMy/mySpr3 should have no errors initially")
                .isFalse();

        // Step 2: Open TheProejct → module TheModel → edit table someRule (rename it)
        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule("TheProejct", "TheModel");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "someRule");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(1, 1, "someRuleModified");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Step 3: Go back to DoliMy → mySpr3 — EPBDS-12848: must still have NO errors
        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule("DoliMy", "DoliMy");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr3");

        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("EPBDS-12848: DoliMy/mySpr3 must have no errors after modifying TheProejct/someRule")
                .isFalse();
    }
}
