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

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAddElementToCollectionSet extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10142")
    @Description("BUG: Error on clicking '+' for input types Collection, Set")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddElementToCollectionSet() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "TestAddElementToCollectionSet.xlsx");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddElementToCollectionSet");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");

        var runMenu = editorPage.getTableToolbarPanelComponent().clickRun();
        runMenu.clickAddElementToCollectionBtn("a =");
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        
        runMenu.clickAddedElementsExpander("a =")
               .clickAddElementToCollectionBtn("d =");
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        
        runMenu.clickAddedElementsExpander("d =");
    }
}