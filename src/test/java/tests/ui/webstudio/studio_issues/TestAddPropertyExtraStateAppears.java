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
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestAddPropertyExtraStateAppears extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11107")
    @Description("'State' property is added to table instead of inherited")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddPropertyExtraStateAppears() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "StudioIssues.TestAddPropertyExtraStateAppears.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Test Project-CW-20200101-20200101");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MyDatatype");
        editorPage.getRightTableDetailsComponent()
                .addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue())
                .setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details")
                .clickSaveBtn();
        WaitUtil.waitForCondition(() -> editorPage.getCenterTable().getCellText(2, 2).equals("description"), 5000, 200, "Waiting for 'description' cell in center table");
        assertThat(editorPage.getCenterTable().getCellText(2, 2)).isEqualTo("description");
        assertThat(editorPage.getCenterTable().getCellText(3, 2)).contains("Result");
        assertThat(editorPage.getCenterTable().getCellText(4, 2)).contains("= new MyDatatype");
    }
}
