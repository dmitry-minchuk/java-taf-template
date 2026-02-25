package tests.ui.webstudio.client;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import tests.BaseTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestLocalZippedProjects extends BaseTest {

    SoftAssert softAssert;
    private static final String ROOT_DIR_PATH = "/Users/dmitryminchuk/Projects/eis/client_projects/сustomers_projects_test_automation_6.x/Glic/GLIC LTD/Current Version - deployment";
    private static final String EXT = ".zip";

    @Test(dataProvider = "Projects")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalZippedProjects(String path1, String path2, String path3) {
        List<String> projectPaths = new ArrayList<>();
        projectPaths.add(path1);
        LOGGER.info("Project path_1: {}", path1);
        if (path2 != null) {
            projectPaths.add(path2);
            LOGGER.info("Project path_2: {}", path2);
        }
        if (path3 != null) {
            projectPaths.add(path3);
            LOGGER.info("Project path_3: {}", path3);
        }

        List<String> projectNames = new ArrayList<>();
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));

        for (String projectPath : projectPaths) {
            LOGGER.info("Project path:{}", projectPath);
            projectNames.add(createProjectFromZipFile(projectPath));
        }

        for (String nameProject : projectNames) {
            editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
            editorPage.navigateToProjectsInBreadcrumbs();
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProject);
            List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(nameProject);

            if (!modules.isEmpty()) {
                editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProject, modules.getFirst());
                editorPage.getProjectModuleDetailsComponent().isVisible();

                if(editorPage.getProblemsPanelComponent().hasErrors()) {
                    List<String> allErrors = editorPage.getProblemsPanelComponent().getAllErrors();

                    StringBuilder errorDetails = new StringBuilder();
                    errorDetails.append(String.format("\nCompilation errors detected in project: %s", nameProject));
                    errorDetails.append(String.format("\nProjects location:\n%s\n", StringUtil.prettyPrintObjectList.apply(projectPaths)));
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
                        testFailureDetails.append(String.format("\nProjects location:\n%s\n", StringUtil.prettyPrintObjectList.apply(projectPaths)));
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
    }

    private Map<Integer, List<String>> collectFiles(List<String> pathList) {
        Map<Integer, List<String>> files = new HashMap<>();
        List<File> deploymentFolders = new ArrayList<>();
        Integer i = 0;
        for (String path : pathList) {
            File parentFolder = new File(StringUtil.getPathWithoutFileName(path));
            List<String> zippedProjects;
            if (parentFolder.getAbsolutePath().endsWith("deployment") && !deploymentFolders.contains(parentFolder)) {
                zippedProjects = FileUtils.listFiles(parentFolder, TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                        .stream().filter(f -> f.getAbsolutePath().endsWith(EXT)).map(File::getAbsolutePath).collect(Collectors.toList());
                deploymentFolders.add(parentFolder);
                files.put(i, zippedProjects);
            } else if (!parentFolder.getAbsolutePath().endsWith("deployment")) {
                zippedProjects = new ArrayList<>();
                zippedProjects.add(path);
                files.put(i, zippedProjects);
            }
            i++;
        }
        return files;
    }

    private String createProjectFromZipFile(String absoluteFilePath) {
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getCreateProjectLink().click();
        //String projectName = getPureName(absoluteFilePath); - not setting custom names anymore

        CreateNewProjectComponent createNewProjectComponent = repositoryPage.getCreateNewProjectComponent();
        ZipArchiveComponent zipComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.ZIP_ARCHIVE);
        zipComponent.getFileInputField().sendKeys(absoluteFilePath);
        //zipComponent.getProjectNameField().sleep(1000).fill(projectName); - not setting custom names anymore
        String projectName = zipComponent.getProjectNameField().sleep(1000).getLocator().inputValue();
        zipComponent.getCreateProjectBtn().click();

        if(repositoryPage.getConfigureCommitInfoComponentShade().isVisible(3000))
            repositoryPage.getConfigureCommitInfoComponent().fillCommitInfoWithRandomData();
        repositoryPage.getRefreshBtn().click(10000);
        return projectName;
    }

    private String getPureName(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(0, dotIndex) : fileName;
    }

    @DataProvider(name = "Projects")
    public Object[][] getProjects() {
        List<String> projectsPathList = FileUtils.listFiles(new File(ROOT_DIR_PATH), TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                .stream().filter(p -> p.getAbsolutePath().endsWith(EXT)).map(File::getAbsolutePath).collect(Collectors.toList());
        Map<Integer, List<String>> files = collectFiles(projectsPathList);
        Object[][] dp = new Object[files.size()][3];
        int i = 0;
        for (Map.Entry<Integer, List<String>> entry : files.entrySet()) {
            List<String> value = entry.getValue();
            for (int z = 0; z < value.size(); z++) {
                System.out.println(value.get(z));
                dp[i][z] = value.get(z);
            }
            System.out.println("-------");
            i++;
        }
        return dp;
    }

    @BeforeClass
    public void beforeClass() {
        softAssert = new SoftAssert();
    }

    @AfterClass
    public void afterClass() {
        softAssert.assertAll();
    }
}