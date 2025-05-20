package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAddPropertyInSpreadSheetTable extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7578")
    @Description("BUG: Exception appears in log file on adding properties for Spreadsheet")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddPropertyInSpreadSheetTable() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "TestAddPropertyInSpreadSheetTable.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddPropertyInSpreadSheetTable");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "SpreadsheetTable");
        editorPage.getRightTableDetailsComponent()
                .addProperty(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue())
                .setProperty(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory")
                .getSaveBtn().click();
        assertThat(editorPage.getRightTableDetailsComponent().isPropertySet(RightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory")).isTrue();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
