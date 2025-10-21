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

public class TestGroupingEdgeCases extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-DuplicateDimensionValidationInGrouping")
    @Description("Test that duplicate dimensions cannot be used in grouping")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDuplicateDimensionValidationInGrouping() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create multiple tag types
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(3, "US");
        tagsPage.addTagValue(3, "EU");

        // Try to set grouping with duplicate dimension
        // Level 1: Domain, Level 2: LOB, Level 3: Domain (duplicate)
        boolean result = tagsPage.trySetDuplicateGrouping(1, "Domain", 2, "LOB", 3, "Domain");

        // Should fail or show error
        Assert.assertFalse(result, "System should reject duplicate Domain in grouping levels");

        // Verify error message
        Assert.assertTrue(tagsPage.hasGroupingError("Duplicate"),
                "Should show error message about duplicate dimensions");

        // Verify Level 3 is disabled for Domain selection
        Assert.assertFalse(tagsPage.isGroupingOptionAvailable(3, "Domain"),
                "Domain should not be available in Level 3 since already used in Level 1");

        // Set valid grouping without duplicates
        tagsPage.setProjectGrouping(1, "Domain");
        tagsPage.setProjectGrouping(2, "LOB");
        tagsPage.setProjectGrouping(3, "Region");

        // Verify valid grouping was set
        Assert.assertEquals(tagsPage.getProjectGroupingLevel(1), "Domain",
                "Level 1 grouping should be Domain");
        Assert.assertEquals(tagsPage.getProjectGroupingLevel(2), "LOB",
                "Level 2 grouping should be LOB");
        Assert.assertEquals(tagsPage.getProjectGroupingLevel(3), "Region",
                "Level 3 grouping should be Region");
    }

    @Test
    @TestCaseId("IPBQA-31659-GroupingPersistenceAndCleanup")
    @Description("Test grouping persistence when tags are deleted and new projects created")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGroupingPersistenceAndCleanup() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create tags
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");

        // Setup grouping: Repository > Domain > LOB
        tagsPage.setProjectGrouping(1, "Domain");
        tagsPage.setProjectGrouping(2, "LOB");

        // Create projects with tags
        editorPage = tagsPage.returnToEditorPage();
        editorPage.createProject("PolicyAuto");
        editorPage.openProjectProperties("PolicyAuto");
        editorPage.selectTagValue("Domain", "Policy");
        editorPage.selectTagValue("LOB", "Auto");
        editorPage.saveProjectProperties();
        editorPage.closeProjectProperties();

        editorPage.createProject("ClaimsHome");
        editorPage.openProjectProperties("ClaimsHome");
        editorPage.selectTagValue("Domain", "Claims");
        editorPage.selectTagValue("LOB", "Home");
        editorPage.saveProjectProperties();
        editorPage.closeProjectProperties();

        // Verify grouping is working
        Assert.assertTrue(editorPage.isProjectGroupedBy("Domain"),
                "Projects should be grouped by Domain");
        Assert.assertTrue(editorPage.isProjectGroupedBy("LOB"),
                "Projects should be grouped by LOB within Domain groups");

        // Return to admin and expand/collapse grouping
        editorPage = tagsPage.returnToEditorPage();
        editorPage.collapseProjectGroup("Domain", "Policy");
        Assert.assertTrue(editorPage.isProjectGroupCollapsed("Domain", "Policy"),
                "Domain:Policy group should be collapsed");

        editorPage.expandProjectGroup("Domain", "Policy");
        Assert.assertTrue(editorPage.isProjectGroupExpanded("Domain", "Policy"),
                "Domain:Policy group should be expanded");

        // Now delete Domain tag from admin panel
        editorPage = tagsPage.returnToEditorPage();
        AdminPage adminPage2 = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage2 = adminPage2.navigateToTagsPage();

        tagsPage2.deleteTagType("Domain");

        // Verify grouping was updated
        // Level 1 should now be empty (no Domain), Level 2 should be LOB (moved up from Level 2)
        Assert.assertNotEquals(tagsPage2.getProjectGroupingLevel(1), "Domain",
                "Domain should no longer be in Level 1 after deletion");

        // Return to editor and check projects still exist
        editorPage = tagsPage2.returnToEditorPage();
        Assert.assertTrue(editorPage.projectExists("PolicyAuto"),
                "Project should still exist after tag type deletion");
        Assert.assertTrue(editorPage.projectExists("ClaimsHome"),
                "Project should still exist after tag type deletion");

        // Tags section should still be visible because LOB tag still exists
        Assert.assertTrue(editorPage.areTagsVisible(),
                "Tags section should still be visible with remaining LOB tag");

        // Create new project - tags section should be available
        editorPage.createProject("NewProject");
        editorPage.openProjectProperties("NewProject");
        Assert.assertTrue(editorPage.hasTagType("LOB"),
                "LOB tag should still be available for new project");
        Assert.assertFalse(editorPage.hasTagType("Domain"),
                "Domain tag should not be available after deletion");
        editorPage.closeProjectProperties();

        // Now delete all remaining tags
        editorPage = tagsPage2.returnToEditorPage();
        AdminPage adminPage3 = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage3 = adminPage3.navigateToTagsPage();

        tagsPage3.deleteTagType("LOB");

        // All tags deleted - verify grouping is cleared
        Assert.assertNull(tagsPage3.getProjectGroupingLevel(1),
                "Grouping Level 1 should be empty after all tags deleted");
        Assert.assertNull(tagsPage3.getProjectGroupingLevel(2),
                "Grouping Level 2 should be empty after all tags deleted");

        // Return to editor - tags section should disappear
        editorPage = tagsPage3.returnToEditorPage();
        Assert.assertFalse(editorPage.areTagsVisible(),
                "Tags section should not be visible when no tags exist");

        // Existing projects should still exist but with no tags
        Assert.assertTrue(editorPage.projectExists("PolicyAuto"),
                "Project should still exist after all tags deleted");
        editorPage.openProjectProperties("PolicyAuto");
        Assert.assertFalse(editorPage.areTagsVisible(),
                "Tags section should not be visible in project properties when no tags exist");
        editorPage.closeProjectProperties();
    }
}
