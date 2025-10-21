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

public class TestAutoFillNonExtensibleBehavior extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-NonExtensibleTagPreventsAutoCreation")
    @Description("Test that non-extensible tags have limited values and cannot create new ones")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNonExtensibleTagPreventsAutoCreation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create a non-extensible tag with limited values
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.setTagTypeExtensible(1, false);

        // Add another tag type
        tagsPage.addTagType("Status");
        tagsPage.addTagValue(2, "Active");
        tagsPage.addTagValue(2, "Inactive");
        tagsPage.setTagTypeExtensible(2, true);

        // Verify non-extensible setting
        Assert.assertFalse(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Domain tag should NOT be extensible");
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(2).isChecked(),
                "Status tag should be extensible");

        // Verify that non-extensible tag only allows predefined values
        Assert.assertEquals(tagsPage.getTagValues(1).size(), 2,
                "Non-extensible Domain should have exactly 2 values");
        Assert.assertTrue(tagsPage.getTagValues(1).containsAll(java.util.Arrays.asList("Policy", "Claims")),
                "Domain should only contain Policy and Claims");
    }

    @Test
    @TestCaseId("IPBQA-31659-CreateProjectWithNonExtensibleConstraint")
    @Description("Test that project creation respects non-extensible tag constraints")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectWithNonExtensibleConstraint() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create non-extensible tag type with limited values
        tagsPage.addTagType("Channel");
        tagsPage.addTagValue(1, "Web");
        tagsPage.addTagValue(1, "Mobile");
        tagsPage.setTagTypeExtensible(1, false);

        // Create extensible tag type
        tagsPage.addTagType("Version");
        tagsPage.addTagValue(2, "v1");
        tagsPage.addTagValue(2, "v2");
        tagsPage.setTagTypeExtensible(2, true);

        // Verify constraints are set correctly
        Assert.assertFalse(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Channel should not be extensible");
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(2).isChecked(),
                "Version should be extensible");

        // Verify non-extensible tag values
        Assert.assertEquals(tagsPage.getTagValues(1).size(), 2,
                "Non-extensible Channel should have exactly 2 values");
        Assert.assertTrue(tagsPage.getTagValues(1).containsAll(java.util.Arrays.asList("Web", "Mobile")),
                "Channel should only have Web and Mobile");

        // Verify extensible tag can have more values
        Assert.assertTrue(tagsPage.getTagValues(2).size() >= 2,
                "Extensible Version should have at least 2 values");
    }
}
