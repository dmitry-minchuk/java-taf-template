package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareGitRevisionsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitSortingExcelFilesInComparePopUp extends BaseTest {

    private static final String PROJECT_NAME = "TestGitSortingExcelFilesInComparePopUp";
    private static final String ZIP_FILE_NAME = "Repository.TestGitSortingExcelFilesInComparePopUp.zip";
    private static final List<String> EXPECTED_ORDER = Arrays.asList(
            "AB_CD.xlsx",
            "AS_CD.xlsx",
            "BC_CD.xlsx",
            "BC_CE.xlsx",
            "DS_CD.xlsx",
            "Input data example for EPBDS-6887 (2).xlsx",
            "Main.xlsx",
            "Main (9).xlsx",
            "PN_CD.xlsx",
            "WY_CD.xlsx",
            "XXD_CD.xlsx",
            "aa_CD.xlsx"
    );

    @Test
    @TestCaseId("IPBQA-27534")
    @Description("Git - Verify Excel files are sorted correctly in Compare popup")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitSortingExcelFilesInComparePopUp() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from ZIP file
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, ZIP_FILE_NAME);

        // Select project in tree
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        // Open Compare dialog
        CompareGitRevisionsDialogComponent compareDialog = repositoryPage.getRepositoryContentButtonsPanelComponent()
                .clickCompareBtn();
        compareDialog.waitForDialogToAppear();

        // Verify left and right modules lists are sorted correctly
        List<String> leftModulesList = compareDialog.getLeftModulesList();
        List<String> rightModulesList = compareDialog.getRightModulesList();

        assertThat(leftModulesList)
                .as("Left modules list should be sorted correctly")
                .isEqualTo(EXPECTED_ORDER);

        assertThat(rightModulesList)
                .as("Right modules list should be sorted correctly")
                .isEqualTo(EXPECTED_ORDER);

        // Close Compare dialog
        compareDialog.close();
    }
}
