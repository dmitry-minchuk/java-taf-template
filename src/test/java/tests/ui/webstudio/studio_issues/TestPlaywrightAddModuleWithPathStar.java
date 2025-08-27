package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAddModuleWithPathStar extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7790")
    @Description("Test module with asterisk path handling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddModuleWithPathStar() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectFromZip(User.ADMIN, "RulesEditor.TestRulesXMLContainsAsterisk.zip");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "EquityScore");

        assertThat(editorPage.getCenterTable().getCellText(1, 1)).contains("SimpleLookup");
        assertThat(String.join("", editorPage.getProblemsPanelComponent().getAllErrors())).contains("ExcelParseException: Unknown file format");
    }
}