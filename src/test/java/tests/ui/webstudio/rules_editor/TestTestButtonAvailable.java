package tests.ui.webstudio.rules_editor;

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
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTestButtonAvailable extends BaseTest {

    private static final String NAME_PROJECT_MY = "MyProject";

    @Test
    @TestCaseId("IPBQA-31701")
    @Description("Project Compilation - Test button available, run tests, copy module, compilation progress for large project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTestButtonAvailable() {
        String nameExample3Project = "Example 3";
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameExample3Project, "Example 3 - Auto Policy Calculation");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyCalculation");
        EditorPage editorPageRef = editorPage;
        WaitUtil.waitForCondition(
                () -> editorPageRef.getEditorToolbarPanelComponent().isTestButtonVisible(),
                5000, 500, "Waiting for Test button to become visible"
        );
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "AccidentPremium");
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "AccidentPremium");
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 3'")
                .isEqualTo("Test 3");

        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        EditorPage editorPageRef2 = editorPage;
        WaitUtil.waitForCondition(
                () -> editorPageRef2.getEditorToolbarPanelComponent().isTestButtonVisible(),
                5000, 500, "Waiting for Test button after full refresh"
        );
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 3' after full refresh")
                .isEqualTo("Test 3");

        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameExample3Project);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyTests");
        var copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName("AutoPolicyTests2");
        copyModuleDialog.clickCopy();

        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameExample3Project);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyTests2");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "DriverPremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DetermineDriverPremium DriverPremiumTest1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "PolicyPremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DeterminePolicyPremium PolicyPremiumTest1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "VehiclePremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DetermineVehiclePremium VehiclePremiumTest1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 6' after copying module and adding tests")
                .isEqualTo("Test 6");

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 6' after refresh")
                .isEqualTo("Test 6");

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_MY, "MyProject.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameExample3Project);
        LocalDriverPool.getPage().goBack();
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_MY, "module_KS");
        editorPage.getProblemsPanelComponent().waitForCompilationProgressBarToContain("Loaded", 200000);
    }
}
