package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReturnCellsMarksWithStar extends BaseTest {

    private static final String PROJECT_NAME = "TestReturnCellsMarksWithStar";

    @Test
    @TestCaseId("EPBDS-8176")
    @Description("Rules Editor - Verify that return cells in Spreadsheet table are marked with star (★)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testReturnCellsMarksWithStar() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES,
                PROJECT_NAME, "TestReturnCellsMarksWithStar.xlsx");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, PROJECT_NAME);

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "myTable");

        TableComponent table = editorPage.getCenterTable();

        assertThat(table.getCellText(4, 2))
                .as("Cell (4,2) should contain 'ba'")
                .contains("ba");

        assertThat(table.getCellText(4, 3).replaceAll("\n", ""))
                .as("Cell (4,3) should contain formula with return mark '=a+7  ★'")
                .contains("a+7  ★");

        assertThat(table.getCellText(4, 4))
                .as("Cell (4,4) should contain value with return mark '26  ★'")
                .contains("26  ★");

        assertThat(table.getCellText(4, 5).replaceAll("\n", ""))
                .as("Cell (4,5) should contain formula with return mark '=a★'")
                .contains("a  ★");
    }
}
