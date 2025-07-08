package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestClickOnOpenApiError extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10252")
    @Description("Test clicking on OpenAPI error from the bottom problems panel by text content")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testClickOnOpenApiError() {
        String projectName = WorkflowService.loginCreateProjectFromZipOpenEditor(User.ADMIN, 
                "TestClickOnOpenApiError.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Algorithm");
        
        // Click on the specific OpenAPI error in the problems panel
        editorPage.getProblemsPanelComponent()
                .selectProblemByText("OpenAPI Reconciliation: Expected method is not found for path ");
        
        // Check that "Something went wrong" error is NOT visible
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong."))
                .as("'Something went wrong' message should not be displayed")
                .isFalse();

        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}