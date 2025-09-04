package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightRightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestAddPropertyInSpreadSheetTable extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7578")
    @Description("BUG: Exception appears in log file on adding properties for Spreadsheet")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddPropertyInSpreadSheetTable() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestAddPropertyInSpreadSheetTable.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAddPropertyInSpreadSheetTable");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "SpreadsheetTable");
        editorPage.getRightTableDetailsComponent()
                .addProperty(PlaywrightRightTableDetailsComponent.DropdownOptions.CATEGORY.getValue())
                .setProperty(PlaywrightRightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory")
                .clickSaveBtn();
        assertThat(editorPage.getRightTableDetailsComponent().isPropertySet(PlaywrightRightTableDetailsComponent.DropdownOptions.CATEGORY.getValue(), "newCategory")).isTrue();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}