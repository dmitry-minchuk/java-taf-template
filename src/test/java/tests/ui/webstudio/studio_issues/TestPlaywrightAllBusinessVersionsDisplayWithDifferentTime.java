package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAllBusinessVersionsDisplayWithDifferentTime extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7708")
    @Description("BUG: Only 1 version of table is displayed in WebStudio, if table have 2 versions with Business dimension property that have different time, but not date")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAllBusinessVersionsDisplayWithDifferentTime() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "TestAllBusinessVersionsDisplayWithDifferentTime.xlsx");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestAllBusinessVersionsDisplayWithDifferentTime");
        PlaywrightLeftRulesTreeComponent leftRulesTreeComponent = editorPage.getLeftRulesTreeComponent();
        leftRulesTreeComponent
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .expandFolderInTree("mySpreadsheet");
        assertThat(leftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018 11:00:00 AM]")).isTrue();
        assertThat(leftRulesTreeComponent.isItemExistsInTree("mySpreadsheet [startRequestDate=08/08/2018]")).isTrue();
    }
}