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

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.ZIP_ARCHIVE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddPropertyExtraStateAppears extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11107")
    @Description("'State' property is added to table instead of inherited")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddPropertyExtraStateAppears() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, ZIP_ARCHIVE, "StudioIssues.TestAddPropertyExtraStateAppears.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Test Project-CW-20200101-20200101");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MyDatatype");
        editorPage.getRightTableDetailsComponent()
                .addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue())
                .setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details")
                .getSaveBtn().click();
        assertThat(editorPage.getCenterTable().getCellText(1, 1)).isEqualTo("description");
        assertThat(editorPage.getCenterTable().getCellText(2, 1)).isEqualTo("Result");
        assertThat(editorPage.getCenterTable().getCellText(3, 1)).contains("= new MyDatatype");
    }
}
