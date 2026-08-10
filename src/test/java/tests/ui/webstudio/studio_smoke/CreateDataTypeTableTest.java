package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.CreateTableDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.ProblemsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDataTypeTableTest extends BaseTest {

    private static final String PROJECT_NAME = "CreateDataTypeTableTest_Project";
    private static final String TABLE_NAME = "MyNewDataType";
    private static final String PARAMETER_NAME = "MyNewParameter";
    private static final String XLS_FILE = "CreateDataTypeTableTest.Main.xls";

    @Test
    @TestCaseId("NTC")
    @Description("Migrated test to create a data type table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateDataTypeTable() {
        // 1. Login
        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // 2. Create Project from Excel
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, PROJECT_NAME, XLS_FILE);

        // 3. Navigate back to Editor and select project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "CreateDataTypeTableTest.Main");

        // 4. Open Create Table dialog
        editorPage.getEditorToolbarPanelComponent().clickCreateTable();

        // 5. Create the data type table
        CreateTableDialogComponent createTableDialog = editorPage.getCreateTableDialogComponent();
        createTableDialog.selectType("Datatype Table")
                .clickNext()
                .setTechnicalName(TABLE_NAME)
                .addParameter("BigDecimal", PARAMETER_NAME)
                .save();

        // 6. Verify no problems
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        problemsPanel.checkNoProblems();

        // 7. Verify table content
        TableComponent table = editorPage.getCenterTable();
        assertThat(table.getCellText(1, 1)).as("Header should contain table name").isEqualTo("Datatype " + TABLE_NAME);
        // The React modal titles the datatype's columns, so the fields start one row lower than in the wizard.
        assertThat(table.getCellText(2, 1)).as("Second row should title the type column").isEqualTo("Type");
        assertThat(table.getCellText(2, 2)).as("Second row should title the name column").isEqualTo("Name");
        assertThat(table.getCellText(3, 1)).as("The field's type should be written as entered").isEqualTo("BigDecimal");
        assertThat(table.getCellText(3, 2)).as("The field's name should be written as entered").isEqualTo(PARAMETER_NAME);
    }
}
