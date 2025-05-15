package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddSingleNumberIntoEmptyCell extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7232")
    @Description("BUG: Impossible to add single number into empty cell")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddSingleNumberIntoEmptyCell() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromTemplate(projectName, "Example 1 - Bank Rating");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Decision", "BankLimitIndex");
        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        EditTablePanelComponent editTablePanel = editorPage.getEditTablePanelComponent();
        TableComponent tableComponent = editorPage.getCenterTable();
        tableComponent.clickCell(9, 0);
        editTablePanel.getInsertRowAfterBtn().click();
        tableComponent.doubleClickAndPasteTextToCell(10, 0, "13");
        assertThat(editorPage.getCenterTable().getCellText(10, 0)).isEqualTo("13");
    }
}
