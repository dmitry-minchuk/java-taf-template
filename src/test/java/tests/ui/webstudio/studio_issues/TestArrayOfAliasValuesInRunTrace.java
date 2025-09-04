package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestArrayOfAliasValuesInRunTrace extends BaseTest {

    private final List<String> tables = Arrays.asList("myRule2", "myRule3", "myRule5", "myRule_array", "myRule_x_array",
            "myRule_x", "myRule");

    @Test
    @TestCaseId("EPBDS-7796")
    @Description("BUG: Dropdown with alias values is empty in Run/Trace for array types")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightArrayOfAliasValuesInRunTrace() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestArrayOfAliasValuesInRunTrace.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestArrayOfAliasValuesInRunTrace");

        // Set the filter and expand the folder once before iterating through tables
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision");

        tables.forEach(tableName -> {
            // Select the item from the already expanded folder
            editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Decision", tableName);

            TableToolbarPanelComponent.IPlaywrightRunMenu runMenu = editorPage.getTableToolbarPanelComponent().clickRun();
            runMenu.clickCreateItem()
                    .clickAddElementToCollectionBtn("my =")
                    .clickExpandCollection();
            
            // Verify dropdown in Run menu
            assertThat(runMenu.getAliasDropdownValues())
                    .as("Dropdown for alias values should contain expected values in Run menu for table: " + tableName)
                    .containsExactly("", "bla1", "bla2", "bla3");

            // Switch to Trace and verify again
            TableToolbarPanelComponent.IPlaywrightTraceMenu traceMenu = editorPage.getTableToolbarPanelComponent().clickTrace();
            assertThat(traceMenu.getAliasDropdownValues())
                    .as("Dropdown for alias values should contain expected values in Trace menu for table: " + tableName)
                    .containsExactly("", "bla1", "bla2", "bla3");
        });
    }
}