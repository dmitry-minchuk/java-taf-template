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

public class TestClickDatatypeNotFoundError extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11609")
    @Description("Test clicking on datatype not found error and validating tree selection")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testClickDatatypeNotFoundError() {
        String projectName = WorkflowService.loginCreateProjectFromZipOpenEditor(User.ADMIN, 
                "TestClickDatatypeNotFoundError.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "module_NJ");
        
        // Click on the datatype not found error in the problems panel
        editorPage.getProblemsPanelComponent().selectProblemByText("is not found.");
        
        // Validate that the correct item is selected in the tree
        assertThat(editorPage.getLeftRulesTreeComponent().getSelectedItemText())
                .as("Selected tree item should be 'SmartRule2'")
                .isEqualTo("SmartRule2");

        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}