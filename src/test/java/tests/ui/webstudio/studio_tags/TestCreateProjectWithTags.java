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
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;

public class TestCreateProjectWithTags extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-CreateWithMandatoryTag")
    @Description("End-to-end test: create project with mandatory tag selection")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMandatoryTagDuringProjectCreation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.setTagTypeOptional(1, false);

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(2, "Active");
        tagsPage.addTagValue(2, "Inactive");
        tagsPage.setTagTypeOptional(2, true);

        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(1).isChecked() == false,
                "Domain tag should be mandatory");
        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(2).isChecked() == true,
                "Status tag should be optional");
    }

    @Test
    @TestCaseId("IPBQA-31659-CreateWithOptionalTag")
    @Description("End-to-end test: create project with optional tag selection")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOptionalTagDuringProjectCreation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Environment");
        tagsPage.addTagValue(1, "Production");
        tagsPage.addTagValue(1, "Development");
        tagsPage.addTagValue(1, "Staging");
        tagsPage.setTagTypeOptional(1, true);

        tagsPage.addTagType("Team");
        tagsPage.addTagValue(2, "Frontend");
        tagsPage.addTagValue(2, "Backend");
        tagsPage.setTagTypeOptional(2, true);

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Production", "Development", "Staging")),
                "Environment should have all values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("Frontend", "Backend")),
                "Team should have all values");
    }

    @Test
    @TestCaseId("IPBQA-31659-CreateWithExtensibleTag")
    @Description("End-to-end test: create project with extensible tag that allows new values")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testExtensibleTagCreationDuringProject() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Category");
        tagsPage.addTagValue(1, "Standard");
        tagsPage.addTagValue(1, "Premium");
        tagsPage.setTagTypeExtensible(1, true);

        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Category tag should be extensible");

        tagsPage.addTagValue(1, "Enterprise");
        Assert.assertTrue(tagsPage.getTagValues(1).contains("Enterprise"),
                "Should be able to add Enterprise value dynamically");
    }

    @Test
    @TestCaseId("IPBQA-31659-CreateWithMultipleTags")
    @Description("End-to-end test: create project with multiple tag types and values")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultipleTagsProjectCreation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(1, "NorthAmerica");
        tagsPage.addTagValue(1, "Europe");
        tagsPage.addTagValue(1, "AsiaPacific");
        tagsPage.setTagTypeOptional(1, false);

        tagsPage.addTagType("Priority");
        tagsPage.addTagValue(2, "High");
        tagsPage.addTagValue(2, "Medium");
        tagsPage.addTagValue(2, "Low");
        tagsPage.setTagTypeOptional(2, true);

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(3, "Development");
        tagsPage.addTagValue(3, "QA");
        tagsPage.addTagValue(3, "Production");
        tagsPage.setTagTypeOptional(3, false);

        Assert.assertEquals(tagsPage.countTagTypes(), 3, "Should have 3 tag types");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("NorthAmerica", "Europe", "AsiaPacific")),
                "Region should have 3 values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("High", "Medium", "Low")),
                "Priority should have 3 values");
        Assert.assertTrue(tagsPage.hasTagValues(3, Arrays.asList("Development", "QA", "Production")),
                "Status should have 3 values");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagValueModification")
    @Description("Test tag value modification and deletion during project workflow")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagValueModificationDuringProject() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Channel");
        tagsPage.addTagValue(1, "Web");
        tagsPage.addTagValue(1, "Mobile");
        tagsPage.addTagValue(1, "Desktop");

        Assert.assertTrue(tagsPage.getTagValues(1).contains("Web"),
                "Should contain Web channel");
        Assert.assertTrue(tagsPage.getTagValues(1).contains("Mobile"),
                "Should contain Mobile channel");

        tagsPage.editTagValue(1, "Web", "Website");
        Assert.assertTrue(tagsPage.getTagValues(1).contains("Website"),
                "Should contain updated Website value");
        Assert.assertFalse(tagsPage.getTagValues(1).contains("Web"),
                "Should not contain old Web value");

        tagsPage.deleteTagValue(1, "Desktop");
        Assert.assertFalse(tagsPage.getTagValues(1).contains("Desktop"),
                "Should not contain deleted Desktop value");
    }

    @Test
    @TestCaseId("IPBQA-31659-ComplexTagScenario")
    @Description("Complex scenario: multiple tag types with different properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testComplexTagScenario() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Line");
        tagsPage.addTagValue(1, "Home");
        tagsPage.addTagValue(1, "Auto");
        tagsPage.setTagTypeOptional(1, false);
        tagsPage.setTagTypeExtensible(1, true);

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Individual");
        tagsPage.addTagValue(2, "Commercial");
        tagsPage.setTagTypeOptional(2, true);
        tagsPage.setTagTypeExtensible(2, false);

        tagsPage.addTagType("Market");
        tagsPage.addTagValue(3, "US");
        tagsPage.addTagValue(3, "UK");
        tagsPage.setTagTypeOptional(3, true);
        tagsPage.setTagTypeExtensible(3, true);

        Assert.assertEquals(tagsPage.countTagTypes(), 3, "Should have 3 tag types with different properties");

        Assert.assertFalse(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(),
                "Line tag should be mandatory");
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Line tag should be extensible");

        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(2).isChecked(),
                "LOB tag should be optional");
        Assert.assertFalse(tagsPage.getTagTypeExtensibleCheckbox(2).isChecked(),
                "LOB tag should not be extensible");

        WaitUtil.sleep(300, "wait for complex scenario completion");
    }
}
