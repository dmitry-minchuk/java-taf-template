package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAllBusinessVersionsDisplayWithDifferentTime extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7708")
    @Description("BUG: Only 1 version of table is displayed in WebStudio, if table have 2 versions with Business dimension property that have different time, but not date")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAllBusinessVersionsDisplayWithDifferentTime() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromExcelFile(projectName, "TestAllBusinessVersionsDisplayWithDifferentTime.xlsx");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAllBusinessVersionsDisplayWithDifferentTime");
        LeftRulesTreeComponent leftRulesTreeComponent = editorPage.getLeftRulesTreeComponent();
        leftRulesTreeComponent.setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        leftRulesTreeComponent.expandFolderInTree("Spreadsheet");
        leftRulesTreeComponent.expandFolderInTree("mySpreadsheet");
        assertThat(leftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018 11:00:00 AM]")).isTrue();
        assertThat(leftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018]")).isTrue();
    }
}
