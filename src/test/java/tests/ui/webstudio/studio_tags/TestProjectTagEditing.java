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
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

public class TestProjectTagEditing extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-TagAvailabilityInProperties")
    @Description("Test that defined tag types and values appear in project properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagAvailabilityInProjectProperties() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Priority");
        tagsPage.addTagValue(1, "High");
        tagsPage.addTagValue(1, "Medium");
        tagsPage.addTagValue(1, "Low");

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(2, "Active");
        tagsPage.addTagValue(2, "Inactive");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("High", "Medium", "Low")),
                "Priority tag should have all values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("Active", "Inactive")),
                "Status tag should have all values");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagTypePresenceCheck")
    @Description("Verify tag types are properly created and available")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagTypePresence() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Department");
        tagsPage.addTagValue(1, "Engineering");
        tagsPage.addTagValue(1, "Sales");

        tagsPage.addTagType("Office");
        tagsPage.addTagValue(2, "NewYork");
        tagsPage.addTagValue(2, "London");

        int tagCount = tagsPage.countTagTypes();
        Assert.assertTrue(tagCount >= 2, "Should have at least 2 tag types");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagValueSelection")
    @Description("Test tag value selection and retrieval in properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagValueSelection() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Severity");
        tagsPage.addTagValue(1, "Critical");
        tagsPage.addTagValue(1, "Major");
        tagsPage.addTagValue(1, "Minor");

        List<String> severityValues = tagsPage.getTagValues(1);
        Assert.assertTrue(severityValues.contains("Critical"), "Should contain Critical");
        Assert.assertTrue(severityValues.contains("Major"), "Should contain Major");
        Assert.assertTrue(severityValues.contains("Minor"), "Should contain Minor");
    }

    @Test
    @TestCaseId("IPBQA-31659-MultipleTagTypes")
    @Description("Test handling of multiple tag types simultaneously")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultipleTagTypes() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Tier");
        tagsPage.addTagValue(1, "Gold");
        tagsPage.addTagValue(1, "Silver");

        tagsPage.addTagType("Version");
        tagsPage.addTagValue(2, "v1");
        tagsPage.addTagValue(2, "v2");

        tagsPage.addTagType("Release");
        tagsPage.addTagValue(3, "Stable");
        tagsPage.addTagValue(3, "Beta");

        Assert.assertEquals(tagsPage.countTagTypes(), 3, "Should have 3 tag types");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Gold", "Silver")),
                "Tier should have Gold and Silver");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("v1", "v2")),
                "Version should have v1 and v2");
        Assert.assertTrue(tagsPage.hasTagValues(3, Arrays.asList("Stable", "Beta")),
                "Release should have Stable and Beta");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagOptionalityBehavior")
    @Description("Test optional vs mandatory tag behavior")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagOptionalityBehavior() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Required");
        tagsPage.addTagValue(1, "Value1");
        tagsPage.setTagTypeOptional(1, false);
        Assert.assertFalse(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(),
                "Required tag should not be optional");

        tagsPage.addTagType("Optional");
        tagsPage.addTagValue(2, "Value2");
        tagsPage.setTagTypeOptional(2, true);
        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(2).isChecked(),
                "Optional tag should be optional");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagExtensibilityBehavior")
    @Description("Test extensible vs non-extensible tag behavior")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagExtensibilityBehavior() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("FixedOptions");
        tagsPage.addTagValue(1, "Option1");
        tagsPage.addTagValue(1, "Option2");
        tagsPage.setTagTypeExtensible(1, false);
        Assert.assertFalse(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Fixed options tag should not be extensible");

        tagsPage.addTagType("CustomOptions");
        tagsPage.addTagValue(2, "Option1");
        tagsPage.setTagTypeExtensible(2, true);
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(2).isChecked(),
                "Custom options tag should be extensible");
    }
}
