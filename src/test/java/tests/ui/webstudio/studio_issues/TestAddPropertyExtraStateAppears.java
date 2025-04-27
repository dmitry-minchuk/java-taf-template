package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
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

public class TestAddPropertyExtraStateAppears extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11107")
    @Description(" ")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddPropertyExtraStateAppears() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromZipArchive(projectName, "StudioIssues.TestAddPropertyExtraStateAppears.zip");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSeleclorComponent().selectModule(projectName, "Test Project-CW-20200101-20200101");
        editorPage.getLeftRulesTreeComponent().setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getLeftRulesTreeComponent().selectItemInTree("Decision", "MyDatatype");
        editorPage.getRightTableDetailsComponent().addProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue());
        editorPage.getRightTableDetailsComponent().setProperty(RightTableDetailsComponent.DropdownOptions.DESCRIPTION.getValue(), "Description details");
        editorPage.getRightTableDetailsComponent().getSaveBtn().click();

    }
}
