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

public class TestAddElementToCollectionSet extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10142")
    @Description("BUG: Error on clicking '+' for input types Collection, Set")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddElementToCollectionSet() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromExcelFile(projectName, "TestAddElementToCollectionSet.xlsx");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddElementToCollectionSet");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Spreadsheet", "mySpr");
        editorPage.getTableToolbarPanelComponent().getRunBtn().click();
        editorPage.getTableToolbarPanelComponent().getAddElementToCollectionBtn().format("a =").click();
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        editorPage.getTableToolbarPanelComponent().getAddedElementsExpander().format("a =").click();
        editorPage.getTableToolbarPanelComponent().getAddElementToCollectionBtn().format("d =").click();
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong.")).isFalse();
        editorPage.getTableToolbarPanelComponent().getAddedElementsExpander().format("d =").click();
    }
}
