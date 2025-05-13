package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAddModuleWithPathExistingModule extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11048")
    @Description("BUG: Two modules with the same path can be created")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddModuleWithPathExistingModule() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProjectFromTemplate(projectName, "Sample Project");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getLeftProjectModuleSelectorComponent().selectProject(projectName);
        editorPage.getProjectDetailsComponent().openAddModulePopup();
        editorPage.getAddModulePopupComponent().fillForm("test", "Main.xlsx");
        assertThat(editorPage.getAddModulePopupComponent().isSpecificPropertyShown("Path is already covered with existing module.")).isTrue().as("'Path is already covered with existing module.' text is expected to be shown");
    }
}
