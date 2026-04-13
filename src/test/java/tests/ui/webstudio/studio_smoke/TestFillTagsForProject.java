package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFillTagsForProject extends BaseTest {

    private static final String PROJECT_NAME = "Tag1-TagOpt1-Project";
    private static final String ZIP_FILE = "TestMergeBranchesNoConflicts_NoConflicts.zip";

    private static final String TAG_TYPE_NAME = "Tag";
    private static final String TAG_TYPE_OPT = "TagOpt";
    private static final String NAME_TEMPLATE = "%Tag%-%TagOpt%-*";

    @Test
    @TestCaseId("EPBDS-15749")
    @Description("Tags admin: Save Templates persists template; Fill Tags for Project assigns tags from matching project name")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFillTagsForProjectFromTemplate() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, ZIP_FILE);

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addNewTagType(TAG_TYPE_NAME)
                .addTag(TAG_TYPE_NAME, "Tag1")
                .addTag(TAG_TYPE_NAME, "Tag2")
                .setExtensible(1, false)
                .setNullable(1, true);

        tagsPage.addNewTagType(TAG_TYPE_OPT)
                .addTag(TAG_TYPE_OPT, "TagOpt1")
                .addTag(TAG_TYPE_OPT, "TagOpt2")
                .setExtensible(2, false)
                .setNullable(2, true);

        tagsPage.setProjectNameTemplates(NAME_TEMPLATE)
                .saveTemplates();

        assertThat(tagsPage.getProjectNameTemplates())
                .as("Project Name Templates should persist after Save Templates click")
                .isEqualTo(NAME_TEMPLATE);

        tagsPage.fillTagsForProject();

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        RepositoryContentTabPropertiesComponent properties = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();

        assertThat(properties.getSelectedTagForType(TAG_TYPE_NAME))
                .as("Tag '%s' should be filled from project name segment '%s'", TAG_TYPE_NAME, "Tag1")
                .isEqualTo("Tag1");
        assertThat(properties.getSelectedTagForType(TAG_TYPE_OPT))
                .as("Tag '%s' should be filled from project name segment '%s'", TAG_TYPE_OPT, "TagOpt1")
                .isEqualTo("TagOpt1");
    }
}
