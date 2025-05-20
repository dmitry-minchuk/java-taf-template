package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.EditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddDeleteRowWithoutSaving extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7474")
    @Description("BUG: Added row is deleted incorrectly - the value of the next row is changed")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddDeleteRowWithoutSaving() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, TEMPLATE, "Tutorial 6 - Introduction to Spreadsheet Tables");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Tutorial6 - Intro to Spreadsheet Tables");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "LossFreeDiscount");

        TableToolbarPanelComponent tableToolbar = editorPage.getTableToolbarPanelComponent();
        tableToolbar.getEditBtn().click();
        TableComponent table = editorPage.getCenterTable();
        table.clickCell(4,1);
        EditTablePanelComponent editTablePanel = editorPage.getEditTablePanelComponent();
        editTablePanel.getInsertRowAfterBtn().click();
        table.doubleClickAndPasteTextToCell(5, 0, "444", false);
        editTablePanel.getRemoveRowBtn().click();
        assertThat(table.getCellText(5, 0)).isEqualTo(" ");
        assertThat(table.getCellText(5, 1)).isEqualTo("0%");
        editTablePanel.getSaveChangesBtn().click();
        assertThat(table.getCellText(5, 0)).isEqualTo(" ");
        assertThat(table.getCellText(5, 1)).isEqualTo("0%");
    }
}
