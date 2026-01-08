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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tests.BaseTest;

import java.util.List;

public class TestLocalCentralProjects extends BaseTest {
    SoftAssert softAssert;
    private static List<String> projectNames;

    private static final String APP_URL = "http://192.168.50.5:8090";

    @BeforeClass
    public void setUp() {
        softAssert = new SoftAssert();

        // Initialize Playwright manually BEFORE using LocalDriverPool.getPage()
        // because BaseTest.beforeMethod() is not called yet at @BeforeClass stage
        LocalDriverPool.initializePlaywright(null);

        // Use temporary EditorPage just to read project names
        EditorPage tempEditorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN), APP_URL);
        RepositoryPage repositoryPage = tempEditorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.unlockAllProjects();
        projectNames = repositoryPage.getAllVisibleProjectsInTable();
        LOGGER.info("Found {} projects to test", projectNames.size());

        // Close this temporary browser - each @Test will get a fresh browser from BaseTest.beforeMethod()
        LocalDriverPool.closePlaywright();
    }

    @DataProvider(name = "ProjectNames")
    public Object[][] getProjectNames() {
        return projectNames.stream()
                .map(projectName -> new Object[]{projectName})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "ProjectNames")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalCentralProjects(String nameProject) {
        //TODO: add test run description for ReportPortal with used repositories
        LOGGER.info("Testing project: {}", nameProject);

        // Create fresh EditorPage for this test (BaseTest.beforeMethod() already initialized new Playwright browser)
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN), APP_URL);

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
                Assert.assertFalse(editorPage.getProblemsPanelComponent().hasErrors(), problemsPanelComponentErrorsMsg);
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
                    Assert.assertTrue(editorPage.getTestResultValidationComponent().isTestTablePassed(), testTableResults);
                    LOGGER.error("TEST FAILURES DETECTED: {}", testTableResults);
                }
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void close() {
        softAssert.assertAll();
    }
}