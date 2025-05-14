package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddDeleteRowWithoutSaving extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7474")
    @Description("BUG: Added row is deleted incorrectly - the value of the next row is changed")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddDeleteRowWithoutSaving() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromTemplate(projectName, "Tutorial 6 - Introduction to Spreadsheet Tables");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Tutorial6 - Intro to Spreadsheet Tables");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Decision", "LossFreeDiscount");
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
