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

public class TestTemplateErrorValidation extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-TemplateErrorMessagesValidation")
    @Description("Test template validation with error messages for various error scenarios")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTemplateErrorMessagesValidation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup: Create only Domain tag, no LOB tag
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");

        // Test 1: Try to save template referencing non-existent tag type
        String invalidTemplate1 = "*-%Domain%-%LOB%";
        boolean result1 = tagsPage.trySaveProjectNameTemplate(invalidTemplate1);

        Assert.assertFalse(result1, "Should reject template with non-existent LOB tag");
        Assert.assertTrue(tagsPage.hasTemplateError("Cannot find tag type"),
                "Should show error about missing tag type LOB");
        Assert.assertTrue(tagsPage.hasTemplateError("LOB"),
                "Error message should mention the missing LOB tag");

        // Test 2: Try to save template without any placeholders
        String invalidTemplate2 = "ProjectName";
        boolean result2 = tagsPage.trySaveProjectNameTemplate(invalidTemplate2);

        Assert.assertFalse(result2, "Should reject template without tag placeholders");
        Assert.assertTrue(tagsPage.hasTemplateError("tag type"),
                "Should show error about missing tag type placeholders");
        Assert.assertTrue(tagsPage.hasTemplateError("template"),
                "Should mention template in error message");

        // Test 3: Try to save template with duplicate tag types
        // Create LOB tag so we can use both
        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");

        String invalidTemplate3 = "*-%Domain%-%LOB%-%Domain%";
        boolean result3 = tagsPage.trySaveProjectNameTemplate(invalidTemplate3);

        Assert.assertFalse(result3, "Should reject template with duplicate tag types");
        Assert.assertTrue(tagsPage.hasTemplateError("duplicate"),
                "Should show error about duplicate Domain tag");
        Assert.assertTrue(tagsPage.hasTemplateError("Domain"),
                "Error should mention which tag is duplicated");

        // Test 4: Valid template should save successfully
        String validTemplate = "*-%Domain%-%LOB%";
        boolean result4 = tagsPage.trySaveProjectNameTemplate(validTemplate);

        Assert.assertTrue(result4, "Valid template should save successfully");
        Assert.assertFalse(tagsPage.hasAnyTemplateError(),
                "Should have no errors for valid template");

        // Verify the template was saved
        Assert.assertTrue(tagsPage.hasTemplatePattern(validTemplate),
                "Template should be saved and available");

        // Test 5: Clear template and verify it can be cleared
        tagsPage.clearProjectNameTemplate();
        Assert.assertFalse(tagsPage.hasTemplatePattern(validTemplate),
                "Template should be cleared");

        // Test 6: Template with empty or invalid placeholder syntax
        String invalidTemplate6 = "*-%-%LOB%";
        boolean result6 = tagsPage.trySaveProjectNameTemplate(invalidTemplate6);

        Assert.assertFalse(result6, "Should reject template with empty placeholder");
        Assert.assertTrue(tagsPage.hasTemplateError("placeholder"),
                "Should show error about invalid placeholder syntax");
    }
}
