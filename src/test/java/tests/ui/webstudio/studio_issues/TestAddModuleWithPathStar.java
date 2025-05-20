package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.ZIP_ARCHIVE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddModuleWithPathStar extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7790")
    @Description("Project disappear from UI, if user added new module with path=*")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddModuleWithPathStar() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, ZIP_ARCHIVE, "RulesEditor.TestRulesXMLContainsAsterisk.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "EquityScore");
        assertThat(editorPage.getCenterTable().getCellText(0, 0)).contains("SimpleLookup");
        assertThat(String.join("", editorPage.getProblemsPanelComponent().getAllErrors())).contains("ExcelParseException: Unknown file format");
    }
}
