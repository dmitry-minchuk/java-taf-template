package tests.ui.webstudio.studio_tags;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestMultiRepositoryAutoFill extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-AutoFillWorksAcrossMultipleRepositories")
    @Description("Test that auto-fill processes projects from multiple repositories simultaneously")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAutoFillWorksAcrossMultipleRepositories() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types
        tagsPage.addTagType("Repository");
        tagsPage.addTagValue(1, "Design");
        tagsPage.addTagValue(1, "Production");
        tagsPage.addTagValue(1, "Testing");

        tagsPage.addTagType("Environment");
        tagsPage.addTagValue(2, "Dev");
        tagsPage.addTagValue(2, "Staging");
        tagsPage.addTagValue(2, "Prod");

        // Setup template
        String template = "App-%Repository%-%Environment%";
        tagsPage.saveProjectNameTemplates(template);

        // Create projects in default repository
        editorPage = tagsPage.returnToEditorPage();
        editorPage.createProject("App-Design-Dev");
        editorPage.createProject("App-Production-Prod");

        // Create or switch to second repository
        editorPage.createRepository("Design1");
        editorPage.switchToRepository("Design1");

        // Create projects in second repository
        editorPage.createProject("App-Testing-Staging");
        editorPage.createProject("App-Design-Prod");

        // Verify projects exist in both repositories
        Assert.assertTrue(editorPage.repositoryExists("Default"),
                "Default repository should exist");
        Assert.assertTrue(editorPage.repositoryExists("Design1"),
                "Second repository Design1 should exist");

        // Open auto-fill form
        tagsPage.openAutoFillForm();

        // Verify auto-fill shows projects from BOTH repositories
        int totalProjects = tagsPage.getExtractedProjectCount();
        Assert.assertEquals(totalProjects, 4, "Auto-fill should detect all 4 projects from both repositories");

        // Verify projects from both repositories are listed
        Assert.assertTrue(tagsPage.hasExtractedProject("Default", "App-Design-Dev"),
                "Should include App-Design-Dev from Default repository");
        Assert.assertTrue(tagsPage.hasExtractedProject("Default", "App-Production-Prod"),
                "Should include App-Production-Prod from Default repository");
        Assert.assertTrue(tagsPage.hasExtractedProject("Design1", "App-Testing-Staging"),
                "Should include App-Testing-Staging from Design1 repository");
        Assert.assertTrue(tagsPage.hasExtractedProject("Design1", "App-Design-Prod"),
                "Should include App-Design-Prod from Design1 repository");

        // Verify tags are extracted from project names across repositories
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Repository", "Design"),
                "Should extract Design repository tag");
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Repository", "Production"),
                "Should extract Production repository tag");
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Repository", "Testing"),
                "Should extract Testing repository tag");
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Environment", "Dev"),
                "Should extract Dev environment tag");
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Environment", "Staging"),
                "Should extract Staging environment tag");
        Assert.assertTrue(tagsPage.hasExtractedTagValue("Environment", "Prod"),
                "Should extract Prod environment tag");

        // Select all projects from both repositories
        tagsPage.selectAllExtractedProjects();

        // Apply auto-fill tags
        tagsPage.applyAutoFillTags();

        // Verify tags were applied to projects in both repositories
        // Check Default repository
        editorPage.switchToRepository("Default");
        editorPage.openProjectProperties("App-Design-Dev");
        Assert.assertEquals(editorPage.getSelectedTagValue("Repository"), "Design",
                "Default repo - App-Design-Dev should have Repository=Design");
        Assert.assertEquals(editorPage.getSelectedTagValue("Environment"), "Dev",
                "Default repo - App-Design-Dev should have Environment=Dev");
        editorPage.closeProjectProperties();

        editorPage.openProjectProperties("App-Production-Prod");
        Assert.assertEquals(editorPage.getSelectedTagValue("Repository"), "Production",
                "Default repo - App-Production-Prod should have Repository=Production");
        Assert.assertEquals(editorPage.getSelectedTagValue("Environment"), "Prod",
                "Default repo - App-Production-Prod should have Environment=Prod");
        editorPage.closeProjectProperties();

        // Check second repository
        editorPage.switchToRepository("Design1");
        editorPage.openProjectProperties("App-Testing-Staging");
        Assert.assertEquals(editorPage.getSelectedTagValue("Repository"), "Testing",
                "Design1 repo - App-Testing-Staging should have Repository=Testing");
        Assert.assertEquals(editorPage.getSelectedTagValue("Environment"), "Staging",
                "Design1 repo - App-Testing-Staging should have Environment=Staging");
        editorPage.closeProjectProperties();

        editorPage.openProjectProperties("App-Design-Prod");
        Assert.assertEquals(editorPage.getSelectedTagValue("Repository"), "Design",
                "Design1 repo - App-Design-Prod should have Repository=Design");
        Assert.assertEquals(editorPage.getSelectedTagValue("Environment"), "Prod",
                "Design1 repo - App-Design-Prod should have Environment=Prod");
        editorPage.closeProjectProperties();
    }
}
