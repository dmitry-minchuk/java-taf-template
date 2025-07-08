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

public class TestClickOnErrorFromTheBottom extends BaseTest {

    @Test
    @TestCaseId("EPBDS-9309")
    @Description("Test clicking on error from the bottom problems panel by index")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testClickOnErrorFromTheBottom() {
        String projectName = WorkflowService.loginCreateProjectFromZipOpenEditor(User.ADMIN, 
                "TestClickOnErrorFromTheBottom.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "ContextDatatypes");
        
        // Click on the first problem in the problems panel
        editorPage.getProblemsPanelComponent().selectProblemByIndex(1);
        
        // Check that "Something went wrong" error is NOT visible
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong."))
                .as("'Something went wrong' message should not be displayed")
                .isFalse();
        
        // Validate that error message is present in the top problems panel
        assertThat(editorPage.getEditorMainContentProblemsPanelComponent().isErrorMessagePresent())
                .as("Error message should be present in top problems panel")
                .isTrue();

        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}