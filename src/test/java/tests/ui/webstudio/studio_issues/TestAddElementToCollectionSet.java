package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddElementToCollectionSet extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10142")
    @Description("BUG: Error on clicking '+' for input types Collection, Set")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddElementToCollectionSet() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "TestAddElementToCollectionSet.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddElementToCollectionSet");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");

        TableToolbarPanelComponent tableToolbarPanel = editorPage.getTableToolbarPanelComponent();
        tableToolbarPanel.clickRun()
                .clickAddElementToCollectionBtn("a =");
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        tableToolbarPanel.getRunMenu()
                .clickAddedElementsExpander("a =")
                .clickAddElementToCollectionBtn("d =");
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        tableToolbarPanel.getRunMenu()
                .clickAddedElementsExpander("d =");
    }
}
