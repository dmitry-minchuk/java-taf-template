package tests.ui.webstudio.client;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

public class TestLocalCentralProjectsDataProvider extends BaseTest {
    SoftAssert softAssert = new SoftAssert();

    private static final String OPENL_RATING = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/openl-rating.git";
    private static final String OPENL_CLAIM = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/openl-claim.git";
    private static final String OPENL_POLICY = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/openl-policy.git";
    private static final String OPENL_POLICY_LIFE = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/openl-policy-life.git";
    private static final String OPENL_FINANCIAL = "https://dev2eisgengit02.exigengroup.com/gitlab/genesis/openl-financials.git";
    private static final List<String> repositoryList1 = Arrays.asList(OPENL_RATING, OPENL_CLAIM);
    private static final List<String> repositoryList2 = Arrays.asList(OPENL_POLICY, OPENL_POLICY_LIFE, OPENL_FINANCIAL);
    private static final String MY_GITLAB_LOGIN = System.getProperty("gitlab_login", "");
    private static final String MY_GITLAB_PASSWORD = System.getProperty("gitlab_password", "");
    private static final String GITLAB_BRANCH = "development";

    @Test(dataProvider = "Repositories")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalCentralProjects(List<String> repositoryUrlList) {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoriesPageComponent repositoriesPageComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToRepositoriesPage();

        repositoryUrlList.forEach(repositoryUrl ->
                repositoriesPageComponent.createDesignRepository(repositoryUrl, MY_GITLAB_LOGIN, MY_GITLAB_PASSWORD, GITLAB_BRANCH, User.ADMIN));

        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.unlockAllProjects();
        List<String> projectNames = repositoryPage.getAllVisibleProjectsInTable();

        for (String nameProject : projectNames) {
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProject);
            List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(nameProject);

            if (!modules.isEmpty()) {
                editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProject, modules.get(0));
                editorPage.getProjectModuleDetailsComponent().isVisible();

                if(editorPage.getProblemsPanelComponent().hasErrors()) {
                    List<String> allErrors = editorPage.getProblemsPanelComponent().getAllErrors();

                    StringBuilder errorDetails = new StringBuilder();
                    errorDetails.append(String.format("\nCompilation errors detected in project: %s", nameProject));
                    errorDetails.append(String.format("\nERRORS (%d):\n", allErrors.size()));
                    for (int i = 0; i < allErrors.size(); i++) {
                        errorDetails.append(String.format("  %d. %s\n", i + 1, allErrors.get(i)));
                    }

                    softAssert.assertFalse(editorPage.getProblemsPanelComponent().hasErrors(), errorDetails.toString());
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

                        softAssert.assertTrue(editorPage.getTestResultValidationComponent().isTestTablePassed(), testFailureDetails.toString());
                    }
                }
            }
        }
    }

    @DataProvider(name = "Repositories")
    public Object[][] getProjects() {
        return new Object[][]{{repositoryList1}, {repositoryList2}};
    }

    @AfterClass(alwaysRun = true)
    public void close() {
        softAssert.assertAll();
    }
}