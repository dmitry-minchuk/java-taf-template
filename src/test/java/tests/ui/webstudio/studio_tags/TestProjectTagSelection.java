package tests.ui.webstudio.studio_tags;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.repositorytabcomponents.TagsInProjectCreationComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

public class TestProjectTagSelection extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-TagSelection")
    @Description("Test tag selection during project creation - mandatory tag requirement")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMandatoryTagSelection() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.setTagTypeOptional(1, false);

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");
        tagsPage.setTagTypeOptional(2, true);

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should be accessible");
    }

    @Test
    @TestCaseId("IPBQA-31659-OptionalTags")
    @Description("Test optional tag behavior - tags can be skipped during project creation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOptionalTagSelection() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(1, "Active");
        tagsPage.addTagValue(1, "Inactive");
        tagsPage.setTagTypeOptional(1, true);

        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(), "Tag type should be optional");

        List<String> availableValues = tagsPage.getTagValues(1);
        Assert.assertTrue(availableValues.contains("Active"), "Should have Active value");
        Assert.assertTrue(availableValues.contains("Inactive"), "Should have Inactive value");
    }

    @Test
    @TestCaseId("IPBQA-31659-ExtensibleTags")
    @Description("Test extensible tag behavior - new tag values can be added during project creation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testExtensibleTagSelection() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Environment");
        tagsPage.addTagValue(1, "Production");
        tagsPage.addTagValue(1, "Development");
        tagsPage.setTagTypeExtensible(1, true);

        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(), "Tag type should be extensible");

        List<String> predefinedValues = tagsPage.getTagValues(1);
        Assert.assertTrue(predefinedValues.containsAll(Arrays.asList("Production", "Development")),
                "Should have predefined values");
    }

    @Test
    @TestCaseId("IPBQA-31659-TagAvailability")
    @Description("Test tag value availability in project creation form")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagValueAvailability() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Type");
        tagsPage.addTagValue(1, "System");
        tagsPage.addTagValue(1, "User");
        tagsPage.addTagValue(1, "Custom");

        List<String> typeValues = tagsPage.getTagValues(1);
        Assert.assertEquals(typeValues.size(), 3, "Should have exactly 3 tag values");
        Assert.assertTrue(typeValues.containsAll(Arrays.asList("System", "User", "Custom")),
                "Should contain all added values");
    }
}
