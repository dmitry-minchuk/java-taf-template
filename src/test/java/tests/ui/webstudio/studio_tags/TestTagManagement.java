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

public class TestTagManagement extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659")
    @Description("Tag management test - create, edit, delete tags and manage tag properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagManagement() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag Types and Values section should be visible");

        tagsPage.addTagType("Domain");
        Assert.assertEquals(tagsPage.getTagTypeName(1), "Domain", "Tag type name should be 'Domain'");

        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        List<String> expectedDomainValues = Arrays.asList("Policy", "Claims");
        Assert.assertTrue(tagsPage.hasTagValues(1, expectedDomainValues), "Domain tag should have Policy and Claims values");

        tagsPage.setTagTypeOptional(1, true);
        Assert.assertTrue(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(), "Tag type should be optional");

        tagsPage.setTagTypeExtensible(1, true);
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(), "Tag type should be extensible");

        tagsPage.addTagType("LOB");
        Assert.assertEquals(tagsPage.getTagTypeName(2), "LOB", "Second tag type name should be 'LOB'");

        tagsPage.addTagValue(2, "Benefits");
        tagsPage.addTagValue(2, "L&A");
        tagsPage.addTagValue(2, "P&C");
        List<String> expectedLOBValues = Arrays.asList("Benefits", "L&A", "P&C");
        Assert.assertTrue(tagsPage.hasTagValues(2, expectedLOBValues), "LOB tag should have Benefits, L&A, P&C values");

        tagsPage.setTagTypeOptional(2, true);
        tagsPage.setTagTypeExtensible(2, true);

        Assert.assertEquals(tagsPage.countTagTypes(), 2, "Should have 2 tag types");

        tagsPage.editTagValue(1, "Policy", "Policy_Updated");
        List<String> updatedDomainValues = Arrays.asList("Policy_Updated", "Claims");
        Assert.assertTrue(tagsPage.hasTagValues(1, updatedDomainValues), "Domain tag should have updated value");

        tagsPage.editTagValue(1, "Policy_Updated", "Policy");

        tagsPage.setProjectNameTemplates("*-%Domain%-%LOB%\n*+%Domain%+%LOB%\n*.%Domain%.%LOB%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to save");

        String templateError = tagsPage.getTemplateError();
        Assert.assertTrue(templateError.isEmpty() || !templateError.contains("error"), "Template should be saved without errors");

        tagsPage.deleteTagValue(2, "P&C");
        List<String> reducedLOBValues = Arrays.asList("Benefits", "L&A");
        Assert.assertTrue(tagsPage.hasTagValues(2, reducedLOBValues), "LOB tag should have P&C removed");

        tagsPage.setProjectNameTemplates("");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to clear");

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should still be accessible");
    }

    @Test
    @TestCaseId("IPBQA-31659-Extended")
    @Description("Tag management advanced test - multiple operations on tags")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTagManagementExtended() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Product");
        tagsPage.addTagValue(1, "Auto");
        tagsPage.addTagValue(1, "Home");
        tagsPage.addTagValue(1, "Life");

        List<String> productValues = Arrays.asList("Auto", "Home", "Life");
        Assert.assertTrue(tagsPage.hasTagValues(1, productValues), "Product tag should have all values");

        tagsPage.setTagTypeOptional(1, false);
        Assert.assertFalse(tagsPage.getTagTypeOptionalCheckbox(1).isChecked(), "Product tag type should be mandatory");

        tagsPage.deleteTagValue(1, "Home");
        List<String> reducedValues = Arrays.asList("Auto", "Life");
        Assert.assertTrue(tagsPage.hasTagValues(1, reducedValues), "Product tag should have Home removed");

        tagsPage.addTagValue(1, "Dental");
        Assert.assertTrue(tagsPage.getTagValues(1).contains("Dental"), "Product tag should have Dental added back");

        int tagCount = tagsPage.countTagTypes();
        Assert.assertTrue(tagCount >= 1, "Should have at least 1 tag type");

        tagsPage.setTagTypeExtensible(1, true);
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(), "Product tag type should be extensible");

        tagsPage.deleteTagType(1);
        WaitUtil.sleep(500, "wait for tag type deletion");

        Assert.assertTrue(tagsPage.isTagTypesAndValuesSectionVisible(), "Tag management should still be accessible after deletion");
    }
}
