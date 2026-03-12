package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLLockUnlockDeprecated extends BaseTest {

    // BRD TR2: Lock/Unlock must be deprecated from the system.
    // Verify that Lock/Unlock buttons are absent from Repository tab and Editor toolbar.

    private static final String LOCK = "lock";
    private static final String UNLOCK = "unlock";

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Lock/Unlock buttons are deprecated — not present in Repository tab or Editor toolbar")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLockUnlockNotPresentInUI() {
        // ============ Admin setup: create project from template ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();

        // ============ STEP 1: Check Repository tab — no Lock/Unlock in table actions ============
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        List<String> tableActions = repositoryPage.getTableActionTitles(projectName);
        assertThat(tableActions)
                .as("Repository table actions should not contain Lock or Unlock (BRD TR2). Actual actions: %s", tableActions)
                .noneMatch(action -> action.toLowerCase().contains(LOCK) || action.toLowerCase().contains(UNLOCK));

        // ============ STEP 2: Check Repository right panel — no Lock/Unlock buttons ============
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        RepositoryContentButtonsPanelComponent buttonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();
        List<String> panelButtons = buttonsPanel.getAllVisibleButtonValues();
        assertThat(panelButtons)
                .as("Repository right panel buttons should not contain Lock or Unlock (BRD TR2). Actual buttons: %s", panelButtons)
                .noneMatch(btn -> btn.toLowerCase().contains(LOCK) || btn.toLowerCase().contains(UNLOCK));

        // ============ STEP 3: Check Editor toolbar — no Lock/Unlock actions ============
        repositoryPage.refresh();
        repositoryPage.unlockAllProjects();
        editorPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalDynamicScore");

        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        List<String> toolbarActions = toolbar.getAllVisibleTopToolbarActions();
        assertThat(toolbarActions)
                .as("Editor toolbar should not contain Lock or Unlock (BRD TR2). Actual actions: %s", toolbarActions)
                .noneMatch(action -> action.toLowerCase().contains(LOCK) || action.toLowerCase().contains(UNLOCK));

        // ============ STEP 4: Check Editor More dropdown — no Lock/Unlock options ============
        List<String> moreMenuItems = toolbar.getMoreMenuItems();
        assertThat(moreMenuItems)
                .as("Editor More menu should not contain Lock or Unlock (BRD TR2). Actual items: %s", moreMenuItems)
                .noneMatch(item -> item.toLowerCase().contains(LOCK) || item.toLowerCase().contains(UNLOCK));
    }
}
