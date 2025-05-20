package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddModuleWithPathExistingModule extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11048")
    @Description("BUG: Two modules with the same path can be created")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddModuleWithPathExistingModule() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, TEMPLATE, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectProject(projectName);
        editorPage.getProjectDetailsComponent().openAddModulePopup();
        editorPage.getAddModulePopupComponent().fillForm("test", "Main.xlsx");
        assertThat(editorPage.getAddModulePopupComponent().isSpecificPropertyShown("Path is already covered with existing module.")).isTrue().as("'Path is already covered with existing module.' text is expected to be shown");
    }
}
