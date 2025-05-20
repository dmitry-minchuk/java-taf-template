package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.EditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddSingleNumberIntoEmptyCell extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7232")
    @Description("BUG: Impossible to add single number into empty cell")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddSingleNumberIntoEmptyCell() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, TEMPLATE, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");
        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        EditTablePanelComponent editTablePanel = editorPage.getEditTablePanelComponent();
        TableComponent tableComponent = editorPage.getCenterTable();
        tableComponent.clickCell(9, 0);
        editTablePanel.getInsertRowAfterBtn().click();
        tableComponent.doubleClickAndPasteTextToCell(10, 0, "13");
        assertThat(editorPage.getCenterTable().getCellText(10, 0)).isEqualTo("13");
    }
}
