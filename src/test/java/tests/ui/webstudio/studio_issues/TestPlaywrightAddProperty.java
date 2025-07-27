package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightRightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAddProperty extends BaseTest {

    @Test
    @TestCaseId("EPBDS-6964")
    @Description("Exception occurs on adding property to the table with two columns - Playwright version")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddProperty() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "StudioIssues_TestAddProperty.xlsx");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "StudioIssues_TestAddProperty");
        
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_CATEGORY)
                .expandFolderInTree("Rules")
                .selectItemInFolder("Rules", "SimpleCalc");
                
        editorPage.getRightTableDetailsComponent()
                .addProperty(PlaywrightRightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue())
                .setProperty(PlaywrightRightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details")
                .getSaveBtn().click();
                
        assertThat(editorPage.getRightTableDetailsComponent()
                .isPropertySet(PlaywrightRightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details"))
                .isTrue();
    }
}