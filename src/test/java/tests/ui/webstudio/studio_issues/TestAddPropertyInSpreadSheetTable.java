package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.LogsUtil;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddPropertyInSpreadSheetTable extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7578")
    @Description("BUG: Exception appears in log file on adding properties for Spreadsheet")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddPropertyInSpreadSheetTable() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromExcelFile(projectName, "TestAddPropertyInSpreadSheetTable.xlsx");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddPropertyInSpreadSheetTable");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Spreadsheet", "SpreadsheetTable");
        editorPage.getRightTableDetailsComponent().addProperty(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue());
        editorPage.getRightTableDetailsComponent().setProperty(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory");
        editorPage.getRightTableDetailsComponent().getSaveBtn().click();
        assertThat(editorPage.getRightTableDetailsComponent().isPropertySet(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory"))
                .isTrue();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
