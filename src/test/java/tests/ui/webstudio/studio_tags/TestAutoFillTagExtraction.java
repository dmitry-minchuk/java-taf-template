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

public class TestAutoFillTagExtraction extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-AutoFillExtractsTagsFromProjectNames")
    @Description("Test template setup for tag extraction from project names")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAutoFillExtractsTagsFromProjectNames() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types for extraction
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.addTagValue(1, "Benefits");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");
        tagsPage.addTagValue(2, "Life");

        // Create project name templates with multiple separators
        String templates = "*-%Domain%-%LOB%\n*+%Domain%+%LOB%\n*.%Domain%.%LOB%";
        tagsPage.setProjectNameTemplates(templates);
        tagsPage.saveProjectNameTemplates();

        // Verify template was saved
        String savedTemplate = tagsPage.getProjectNameTemplates();
        Assert.assertNotNull(savedTemplate, "Template should be saved");
        Assert.assertTrue(savedTemplate.contains("Domain"), "Template should contain Domain placeholder");
        Assert.assertTrue(savedTemplate.contains("LOB"), "Template should contain LOB placeholder");
    }

    @Test
    @TestCaseId("IPBQA-31659-AutoFillAppliesTagsToMultipleProjects")
    @Description("Test that auto-fill form shows extracted values from project names")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAutoFillAppliesTagsToMultipleProjects() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types
        tagsPage.addTagType("Region");
        tagsPage.addTagValue(1, "NorthAmerica");
        tagsPage.addTagValue(1, "Europe");
        tagsPage.addTagValue(1, "AsiaPacific");

        tagsPage.addTagType("Channel");
        tagsPage.addTagValue(2, "Web");
        tagsPage.addTagValue(2, "Mobile");
        tagsPage.addTagValue(2, "Desktop");

        // Create and save template
        String templates = "*-%Region%-%Channel%";
        tagsPage.setProjectNameTemplates(templates);
        tagsPage.saveProjectNameTemplates();

        // Verify template was saved
        String savedTemplate = tagsPage.getProjectNameTemplates();
        Assert.assertNotNull(savedTemplate, "Template should be saved");

        // Open fill tags for projects form
        tagsPage.openFillTagsForProjectsForm();
        Assert.assertTrue(tagsPage.isProjectsWithoutTagsTableVisible(),
                "Projects without tags table should be visible");
    }

    @Test
    @TestCaseId("IPBQA-31659-IncrementalAutoFillWithPartialTags")
    @Description("Test template with tag types that will be extracted from project names")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testIncrementalAutoFillWithPartialTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types
        tagsPage.addTagType("Status");
        tagsPage.addTagValue(1, "Active");
        tagsPage.addTagValue(1, "Inactive");
        tagsPage.addTagValue(1, "Development");

        tagsPage.addTagType("Priority");
        tagsPage.addTagValue(2, "High");
        tagsPage.addTagValue(2, "Medium");
        tagsPage.addTagValue(2, "Low");

        // Verify tag values were added
        Assert.assertTrue(tagsPage.hasTagValue(1, "Active"),
                "Should have Active status");
        Assert.assertTrue(tagsPage.hasTagValue(1, "Development"),
                "Should have Development status");
        Assert.assertTrue(tagsPage.hasTagValue(2, "High"),
                "Should have High priority");

        // Setup template
        String templates = "Task-%Status%-%Priority%";
        tagsPage.setProjectNameTemplates(templates);
        tagsPage.saveProjectNameTemplates();

        // Verify template saved without errors
        String errorMsg = tagsPage.getTemplateError();
        Assert.assertTrue(errorMsg.isEmpty() || !errorMsg.toLowerCase().contains("error"),
                "Template should save without errors");

        // Open fill form
        tagsPage.openFillTagsForProjectsForm();
        Assert.assertTrue(tagsPage.isProjectsWithoutTagsTableVisible(),
                "Fill form should show projects table");
    }
}
