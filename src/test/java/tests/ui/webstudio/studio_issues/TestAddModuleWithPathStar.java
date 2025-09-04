package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddModuleWithPathStar extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7790")
    @Description("Test module with asterisk path handling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddModuleWithPathStar() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "RulesEditor.TestRulesXMLContainsAsterisk.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "EquityScore");

        assertThat(editorPage.getCenterTable().getCellText(1, 1)).contains("SimpleLookup");
        assertThat(String.join("", editorPage.getProblemsPanelComponent().getAllErrors())).contains("ExcelParseException: Unknown file format");
    }
}