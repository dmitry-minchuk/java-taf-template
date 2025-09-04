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

public class TestAddElementToCollectionSet extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10142")
    @Description("BUG: Error on clicking '+' for input types Collection, Set")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddElementToCollectionSet() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestAddElementToCollectionSet.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddElementToCollectionSet");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
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