package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDefaultProperties extends BaseTest {

    private static final String PROJECT_NAME = "TestDefaultProperties";

    @Test
    @TestCaseId("IPBQA-27254")
    @Description("Rules Editor - Check default properties (Name field) displayed in table details")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDefaultProperties() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES,
                PROJECT_NAME, "TestDefaultProperties.xlsx");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "TestDefaultProperties");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Run")
                .selectItemInFolder("Run", "SpreadsheetTable");

        assertThat(editorPage.getRightTableDetailsComponent().getPropertiesRowCount())
                .as("Properties table should have 1 row for Run SpreadsheetTable")
                .isEqualTo(1);

        assertThat(editorPage.getRightTableDetailsComponent().getPropertyNameInRow(1))
                .as("First property should be 'Name' for Run SpreadsheetTable")
                .contains("Name");

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SpreadsheetTable");

        assertThat(editorPage.getRightTableDetailsComponent().getPropertiesRowCount())
                .as("Properties table should have 1 row for Test SpreadsheetTable")
                .isEqualTo(1);

        assertThat(editorPage.getRightTableDetailsComponent().getPropertyNameInRow(1))
                .as("First property should be 'Name' for Test SpreadsheetTable")
                .contains("Name");
    }
}
