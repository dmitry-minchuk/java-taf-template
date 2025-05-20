package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddProperty extends BaseTest {

    @Test
    @TestCaseId("EPBDS-6964")
    @Description("Exception occurs on adding property to the table with two columns")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddProperty() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "StudioIssues_TestAddProperty.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "StudioIssues_TestAddProperty");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_CATEGORY)
                .expandFolderInTree("Rules")
                .selectItemInFolder("Rules", "SimpleCalc");
        editorPage.getRightTableDetailsComponent()
                .addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue())
                .setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details")
                .getSaveBtn().click();
        assertThat(editorPage.getRightTableDetailsComponent().isPropertySet(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details"))
                .isTrue();
    }
}
