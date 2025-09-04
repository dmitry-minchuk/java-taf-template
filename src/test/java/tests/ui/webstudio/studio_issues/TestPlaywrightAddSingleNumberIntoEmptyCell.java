package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.ui.webstudio.components.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightEditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAddSingleNumberIntoEmptyCell extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7232")
    @Description("BUG: Impossible to add single number into empty cell")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddSingleNumberIntoEmptyCell() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");
        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        PlaywrightEditTablePanelComponent editTablePanel = editorPage.getEditTablePanelComponent();
        TableComponent tableComponent = editorPage.getCenterTable();
        tableComponent.clickCell(10, 1);
        editTablePanel.clickInsertRowAfter();
        tableComponent.editCell(11, 1, "13", true);
        assertThat(editorPage.getCenterTable().getCellText(11, 1)).isEqualTo("13");
    }
}