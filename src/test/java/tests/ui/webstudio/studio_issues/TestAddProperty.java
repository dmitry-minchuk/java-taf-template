package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestAddProperty extends BaseTest {

    @Test
    @TestCaseId("EPBDS-6964")
    @Description("Exception occurs on adding property to the table with two columns - Playwright version")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddProperty() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "StudioIssues_TestAddProperty.xlsx");
        EditorPage editorPage = new EditorPage();
        
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "StudioIssues_TestAddProperty");
        
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_CATEGORY)
                .expandFolderInTree("Rules")
                .selectItemInFolder("Rules", "SimpleCalc");
                
        editorPage.getRightTableDetailsComponent()
                .addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue())
                .setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details")
                .clickSaveBtn();
                
        assertThat(editorPage.getRightTableDetailsComponent()
                .isPropertySet(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details"))
                .isTrue();
    }
}