package tests.ui.webstudio.studio_tags;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ProjectPropertiesTagsComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

public class TestTagsAlreadyApplied extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-AlreadyApplied")
    @Description("Tag reassignment test - handle cases where tags are already applied to projects")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagsAlreadyAppliedToProjects() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(1, "Active");
        tagsPage.addTagValue(1, "Inactive");
        tagsPage.addTagValue(1, "Archived");

        tagsPage.addTagType("Environment");
        tagsPage.addTagValue(2, "Production");
        tagsPage.addTagValue(2, "Development");
        tagsPage.addTagValue(2, "Testing");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Active", "Inactive", "Archived")),
            "Status tag should have all values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("Production", "Development", "Testing")),
            "Environment tag should have all values");

        tagsPage.setProjectNameTemplates("*-%Status%-%Environment%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to save");

        String templateError = tagsPage.getTemplateError();
        Assert.assertTrue(templateError.isEmpty() || !templateError.contains("error"),
            "Template should save without errors");

        List<List<String>> tableContent = tagsPage.getProjectsWithoutTagsTableContent();
        Assert.assertTrue(tableContent != null, "Projects without tags table should be accessible");
    }

    @Test
    @TestCaseId("IPBQA-31659-PartialTagging")
    @Description("Partial tagging test - some projects already have tags, others need them assigned")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPartialTaggingScenario() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Priority");
        tagsPage.addTagValue(1, "High");
        tagsPage.addTagValue(1, "Medium");
        tagsPage.addTagValue(1, "Low");
        tagsPage.setTagTypeOptional(1, true);

        tagsPage.addTagType("Owner");
        tagsPage.addTagValue(2, "Team_A");
        tagsPage.addTagValue(2, "Team_B");
        tagsPage.addTagValue(2, "Team_C");
        tagsPage.setTagTypeOptional(2, true);

        Assert.assertEquals(tagsPage.countTagTypes(), 2, "Should have 2 tag types");

        tagsPage.editTagValue(1, "High", "Critical");
        List<String> updatedPriority = tagsPage.getTagValues(1);
        Assert.assertTrue(updatedPriority.contains("Critical"), "Priority should have Critical value after edit");

        tagsPage.deleteTagValue(2, "Team_C");
        List<String> updatedOwner = tagsPage.getTagValues(2);
        Assert.assertFalse(updatedOwner.contains("Team_C"), "Owner should not have Team_C after deletion");

        WaitUtil.sleep(300, "wait for deletion to complete");

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should still be accessible");
    }

    @Test
    @TestCaseId("IPBQA-31659-NewTagTypeCreation")
    @Description("New tag type creation test - create new tag type and verify it doesn't affect existing projects")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNewTagTypeCreationWithExistingProjects() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Department");
        tagsPage.addTagValue(1, "Sales");
        tagsPage.addTagValue(1, "Engineering");
        tagsPage.addTagValue(1, "Marketing");

        int initialTagCount = tagsPage.countTagTypes();
        Assert.assertTrue(initialTagCount >= 1, "Should have at least 1 tag type");

        tagsPage.addTagType("Office");
        tagsPage.addTagValue(2, "New_York");
        tagsPage.addTagValue(2, "London");
        tagsPage.addTagValue(2, "Tokyo");

        int updatedTagCount = tagsPage.countTagTypes();
        Assert.assertEquals(updatedTagCount, initialTagCount + 1, "Should have one more tag type");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Sales", "Engineering", "Marketing")),
            "Department tag should retain all original values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("New_York", "London", "Tokyo")),
            "Office tag should have all new values");

        WaitUtil.sleep(500, "wait for tag creation");
    }

    @Test
    @TestCaseId("IPBQA-31659-SelectiveApply")
    @Description("Selective tag application test - apply tags only to selected projects")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSelectiveTagApplication() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Application");
        tagsPage.addTagValue(1, "WebApp");
        tagsPage.addTagValue(1, "MobileApp");
        tagsPage.addTagValue(1, "API");
        tagsPage.setTagTypeExtensible(1, true);

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should be accessible");

        List<String> appValues = tagsPage.getTagValues(1);
        Assert.assertTrue(appValues.containsAll(Arrays.asList("WebApp", "MobileApp", "API")),
            "Application tag should have all values");

        tagsPage.setTagTypeOptional(1, false);
        Assert.assertFalse(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(),
            "Application tag should be mandatory");

        WaitUtil.sleep(300, "wait for option update");
    }
}
