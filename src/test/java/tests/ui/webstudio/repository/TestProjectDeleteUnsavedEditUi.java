package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectDeleteUnsavedEditUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-16229")
    @Description("Known-failing regression for EPBDS-16229: deleting a project must succeed even when its name was changed in the edit dialog but the project was not saved. Stays red until EPBDS-16229 is fixed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeleteProjectWithUnsavedRulesEditSucceeds() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        String renamedName = projectName + "Renamed";
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        editorPage.openEditProjectDialog(projectName)
                .setProjectName(renamedName)
                .clickUpdateButton();

        // The React projects list keys a row by its working-copy name, so after the unsaved rename the
        // row (and its Delete action) is found under the new name, not the original.
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.deleteProject(renamedName)
                .enterDeletionComment("Removed by automated regression test")
                .acknowledgePermanentDeletion()
                .attemptDelete();
        repositoryPage.reloadPage();

        assertThat(repositoryPage.isProjectPresent(renamedName))
                .as("A project with an unsaved rules.xml edit must delete successfully, not error out")
                .isFalse();
    }
}
