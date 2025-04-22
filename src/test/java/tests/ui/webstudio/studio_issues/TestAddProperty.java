package tests.ui.webstudio.studio_issues;

import configuration.annotations.AppContainerConfig;
import configuration.annotations.JiraTicket;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddProperty extends BaseTest {

    @Test
    @JiraTicket("EPBDS-6964")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddProperty() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromExcelFile(projectName, "StudioIssues_TestAddProperty.xlsx");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSeleclorComponent().selectModule(projectName, "StudioIssues_TestAddProperty");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_CATEGORY);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Rules", "SimpleCalc");
        assertThat(4).isGreaterThan(5);
        editorPage.getRightTableDetailsComponent().addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue());
        editorPage.getRightTableDetailsComponent().setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details");
        editorPage.getRightTableDetailsComponent().getSaveBtn().click();
        assertThat(editorPage.getRightTableDetailsComponent().isPropertySet(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details"))
                .isTrue();
    }

}
