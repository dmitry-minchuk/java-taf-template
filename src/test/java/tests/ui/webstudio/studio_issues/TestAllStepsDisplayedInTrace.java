package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.ZIP_ARCHIVE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAllStepsDisplayedInTrace extends BaseTest {

    private final List<String> EXPECTED_TRACE_TREE_ITEMS = Arrays.asList(
            "$Formula$Coverage_ID = null", "$Formula$UnitArea = null", "$Formula$Coverage_Name = null",
            "$Formula$Coverage_Level = null", "$Formula$Coverage_Previous_Time = null",
            "$Formula$Coverage_Previous_Revision = null", "$Formula$Coverage_Rules = null",
            "$Formula$Applicable_Strategy_Rules = null", "$Formula$Inactive_Rules = null",
            "$Formula$FeeCurrentPA = {}", "$Formula$Coverage_Active_Rules_Amounts = {}",
            "$Formula$Coverage_Inactive_Rules_Amounts = {}"
    );

    @Test
    @TestCaseId("EPBDS-8215")
    @Description("BUG: Some Steps are not displayed in Trace")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAllStepsDisplayedInTrace() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, ZIP_ARCHIVE, "TestAllStepsDisplayedInTrace.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "CO-rules");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "DetermineCoverageFeeOrTax2");

        editorPage.getTableToolbarPanelComponent()
                .clickTrace()
                .setFactorTextField("123")
                .clickTraceInsideMenu();
        List<String> visibleItemsFromTree = editorPage.getTableToolbarPanelComponent()
                .getTraceWindow()
                .expandItemInTree(0)
                .getVisibleItemsFromTree();
        assertThat(visibleItemsFromTree.subList(1, 13)).containsAll(EXPECTED_TRACE_TREE_ITEMS);
    }
}
