package tests.ui.webstudio.client;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tests.BaseTest;

import java.util.List;

public class TestLocalCentralProjects extends BaseTest {
    SoftAssert softAssert = new SoftAssert();

    private static final String APP_URL = "http://192.168.50.5:8090";

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalCentralProjects() {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN), APP_URL);
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.unlockAllProjects();
        List<String> projectNames = repositoryPage.getAllVisibleProjectsInTable();

        for (String nameProject : projectNames) {
            editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProject);
            List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(nameProject);

            if (!modules.isEmpty()) {
                editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProject, modules.getFirst());
                editorPage.getProjectModuleDetailsComponent().isVisible();

                if(editorPage.getProblemsPanelComponent().hasErrors()) {
                    List<String> allErrors = editorPage.getProblemsPanelComponent().getAllErrors();

                    StringBuilder errorDetails = new StringBuilder();
                    errorDetails.append(String.format("\nCompilation errors detected in project: %s", nameProject));
                    errorDetails.append(String.format("\nERRORS (%d):\n", allErrors.size()));
                    for (int i = 0; i < allErrors.size(); i++) {
                        errorDetails.append(String.format("  %d. %s\n", i + 1, allErrors.get(i)));
                    }

                    String problemsPanelComponentErrorsMsg = errorDetails.toString();
                    softAssert.assertFalse(editorPage.getProblemsPanelComponent().hasErrors(), problemsPanelComponentErrorsMsg);
                    LOGGER.error("COMPILATION ERROR DETECTED: {}", problemsPanelComponentErrorsMsg);
                }

                if (editorPage.getEditorToolbarPanelComponent().getTestDropdownBtn().isVisible()) {
                    editorPage.getEditorToolbarPanelComponent()
                            .clickTestDropdown()
                            .runTests();
                    editorPage.waitUntilSpinnerLoaded();
                    if(!editorPage.getTestResultValidationComponent().isTestTablePassed()) {
                        List<String> failedTests = editorPage.getTestResultValidationComponent().getAllFailedTests();

                        StringBuilder testFailureDetails = new StringBuilder();
                        testFailureDetails.append(String.format("\nTest failures detected in project: %s", nameProject));
                        testFailureDetails.append(String.format("\nFAILED TESTS (%d):\n", failedTests.size()));
                        for (int i = 0; i < failedTests.size(); i++) {
                            testFailureDetails.append(String.format("  %d. %s\n", i + 1, failedTests.get(i)));
                        }

                        String testTableResults = testFailureDetails.toString();
                        softAssert.assertTrue(editorPage.getTestResultValidationComponent().isTestTablePassed(), testTableResults);
                        LOGGER.error("TEST FAILURES DETECTED: {}", testTableResults);
                    }
                }
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void close() {
        softAssert.assertAll();
    }
}