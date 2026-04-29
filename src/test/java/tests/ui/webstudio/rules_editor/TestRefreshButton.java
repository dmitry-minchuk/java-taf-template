package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.AppContainerFileService;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRefreshButton extends BaseTest {

    private static final String BAD_EXCEL_FILE = "TestRefreshButtonBad.xlsx";
    private static final String GOOD_EXCEL_FILE = "TestRefreshButtonGood.xlsx";
    private static final String EXPECTED_BAD_PROJECT_ERROR = "Encountered \"<EOF>\" at line 1, column 24.";

    @Test
    @TestCaseId("IPBQA-28382")
    @Description("Refresh button picks up project changes made on the user-workspace filesystem outside WebStudio")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRefreshButton() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, BAD_EXCEL_FILE);
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "TestRefreshButtonBad");

        assertThat(editorPage.getProblemsPanelComponent().isErrorPresent(EXPECTED_BAD_PROJECT_ERROR))
                .as("Bad project should produce expected compilation error")
                .isTrue();

        AppContainerFileService.copyFileToProjectWorkspace(
                User.ADMIN.getValue(),
                projectName,
                TestDataUtil.getFilePathFromResources(GOOD_EXCEL_FILE),
                BAD_EXCEL_FILE);

        editorPage.refresh();

        editorPage.getProblemsPanelComponent().checkNoProblems();
    }
}
