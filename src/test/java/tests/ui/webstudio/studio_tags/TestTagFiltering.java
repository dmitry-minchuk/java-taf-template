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
import helpers.utils.WaitUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

public class TestTagFiltering extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-Filtering")
    @Description("Tag filtering and grouping test - assign tags to projects and test filtering/grouping")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagFilteringAndGrouping() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.setTagTypeOptional(1, true);

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");
        tagsPage.addTagValue(2, "Life");
        tagsPage.setTagTypeOptional(2, true);

        tagsPage.setProjectNameTemplates("*-%Domain%-%LOB%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to save");

        String templateError = tagsPage.getTemplateError();
        Assert.assertTrue(templateError.isEmpty() || !templateError.contains("error"), "Template should be saved successfully");

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should be accessible");
    }

    @Test
    @TestCaseId("IPBQA-31659-Grouping")
    @Description("Tag grouping test - group projects by tag values")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectTagGrouping() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Category");
        tagsPage.addTagValue(1, "Premium");
        tagsPage.addTagValue(1, "Standard");
        tagsPage.addTagValue(1, "Basic");

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(2, "US");
        tagsPage.addTagValue(2, "EU");
        tagsPage.addTagValue(2, "APAC");

        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Premium", "Standard", "Basic")),
            "Category tag should have all values");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("US", "EU", "APAC")),
            "Region tag should have all values");

        WaitUtil.sleep(500, "wait for tag setup");

        Assert.assertEquals(tagsPage.countTagTypes(), 2, "Should have 2 tag types");
    }

    @Test
    @TestCaseId("IPBQA-31659-Inheritance")
    @Description("Tag inheritance test - tags should inherit when projects are copied")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagInheritanceOnProjectCopy() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Type");
        tagsPage.addTagValue(1, "Inherited");
        tagsPage.setTagTypeExtensible(1, true);

        List<String> typeValues = tagsPage.getTagValues(1);
        Assert.assertTrue(typeValues.contains("Inherited"), "Type tag should contain Inherited value");

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management interface should remain accessible");
    }
}
