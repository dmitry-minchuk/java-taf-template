package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorRevisionsTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentRevisionsTabComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTabRevisionsInEditor extends BaseTest {

    @Test
    @TestCaseId("IPBQA-30123")
    @Description("Verifies revisions tab in editor: initial revision after project creation, new revision after save, and opening old revision shows original cell value.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTabRevisionsInEditor() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();

        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();
        assertThat(revisionsTab.getRowCount())
                .as("Initial revision count should be 1")
                .isEqualTo(1);
        assertThat(revisionsTab.getCommentForRow(1))
                .as("Initial revision comment should indicate project creation")
                .isEqualTo("Project " + projectName + " is created.");

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 2, "100");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();

        revisionsTab.waitForTableToLoad();
        assertThat(revisionsTab.getRowCount())
                .as("Revision count after save should be 2")
                .isEqualTo(2);
        assertThat(revisionsTab.getCommentForRow(1))
                .as("Most recent revision comment should indicate project save")
                .isEqualTo("Project " + projectName + " is saved.");
        assertThat(revisionsTab.getCommentForRow(2))
                .as("Oldest revision comment should indicate project creation")
                .isEqualTo("Project " + projectName + " is created.");

        // Compare editor revisions count with repository revisions count
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        RepositoryContentRevisionsTabComponent repoRevisionsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectRevisionsTab();
        int repositoryRevisionCount = repoRevisionsTab.getRevisionsCount();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        revisionsTab.waitForTableToLoad();
        assertThat(revisionsTab.getRowCount())
                .as("Editor revisions count should match repository revisions count")
                .isEqualTo(repositoryRevisionCount);

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();

        revisionsTab.waitForTableToLoad();
        assertThat(revisionsTab.getRowCount())
                .as("Revision count should still be 2 after module navigation")
                .isEqualTo(2);

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        repositoryPage.waitUntilSpinnerLoaded();
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();

        revisionsTab.waitForTableToLoad();
        revisionsTab.openRevision(2);
        // TODO: verify viewing revision status - locator unknown

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        assertThat(editorPage.getCenterTable().getCellText(6, 2))
                .as("Cell value should be the original value before the edit when viewing old revision")
                .isEqualTo("0");
    }
}
