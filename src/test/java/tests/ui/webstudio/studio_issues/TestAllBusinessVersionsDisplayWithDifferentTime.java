package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestAllBusinessVersionsDisplayWithDifferentTime extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7708")
    @Description("BUG: Only 1 version of table is displayed in WebStudio, if table have 2 versions with Business dimension property that have different time, but not date")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAllBusinessVersionsDisplayWithDifferentTime() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestAllBusinessVersionsDisplayWithDifferentTime.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAllBusinessVersionsDisplayWithDifferentTime");
        EditorLeftRulesTreeComponent editorLeftRulesTreeComponent = editorPage.getEditorLeftRulesTreeComponent();
        editorLeftRulesTreeComponent.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .expandFolderInTree("mySpreadsheet");
        assertThat(editorLeftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018 11:00:00 AM]")).isTrue();
        assertThat(editorLeftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018]")).isTrue();
    }
}