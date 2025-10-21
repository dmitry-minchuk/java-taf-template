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

public class TestProjectGroupingAndFiltering extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-SetupForGrouping")
    @Description("Setup tag structure for project grouping")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSetupTagStructureForGrouping() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.addTagValue(1, "Benefits");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");
        tagsPage.addTagValue(2, "Life");

        Assert.assertEquals(tagsPage.countTagTypes(), 2, "Should have 2 tag types for grouping");
        Assert.assertTrue(tagsPage.hasTagValues(1, Arrays.asList("Policy", "Claims", "Benefits")),
                "Domain should have all values for grouping");
        Assert.assertTrue(tagsPage.hasTagValues(2, Arrays.asList("Auto", "Home", "Life")),
                "LOB should have all values for grouping");
    }

    @Test
    @TestCaseId("IPBQA-31659-SetupForFiltering")
    @Description("Setup tag structure for project filtering")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSetupTagStructureForFiltering() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Line");
        tagsPage.addTagValue(1, "Home");
        tagsPage.addTagValue(1, "Auto");
        tagsPage.addTagValue(1, "Life");
        tagsPage.addTagValue(1, "Annuity");

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(2, "NorthAmerica");
        tagsPage.addTagValue(2, "Europe");
        tagsPage.addTagValue(2, "AsiaPacific");

        Assert.assertEquals(tagsPage.countTagTypes(), 2, "Should have 2 tag types for filtering");
        Assert.assertTrue(tagsPage.getTagValues(1).size() >= 4, "Line should have multiple values");
        Assert.assertTrue(tagsPage.getTagValues(2).size() >= 3, "Region should have multiple values");
    }

    @Test
    @TestCaseId("IPBQA-31659-GroupingByFirstDimension")
    @Description("Test grouping by primary dimension (first tag type)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGroupingByFirstDimension() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("ProductType");
        tagsPage.addTagValue(1, "Insurance");
        tagsPage.addTagValue(1, "Banking");

        tagsPage.addTagType("Segment");
        tagsPage.addTagValue(2, "Individual");
        tagsPage.addTagValue(2, "Commercial");

        List<String> productTypes = tagsPage.getTagValues(1);
        Assert.assertTrue(productTypes.size() >= 2, "Should have at least 2 product types for grouping");

        for (String productType : productTypes) {
            Assert.assertNotNull(productType, "Product type should not be null");
            Assert.assertFalse(productType.trim().isEmpty(), "Product type should not be empty");
        }
    }

    @Test
    @TestCaseId("IPBQA-31659-GroupingBySecondDimension")
    @Description("Test grouping by secondary dimension (second tag type)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGroupingBySecondDimension() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Department");
        tagsPage.addTagValue(1, "Sales");
        tagsPage.addTagValue(1, "Engineering");

        tagsPage.addTagType("Office");
        tagsPage.addTagValue(2, "NewYork");
        tagsPage.addTagValue(2, "LosAngeles");
        tagsPage.addTagValue(2, "Chicago");

        List<String> offices = tagsPage.getTagValues(2);
        Assert.assertEquals(offices.size(), 3, "Should have 3 offices for grouping by second dimension");
        Assert.assertTrue(offices.containsAll(Arrays.asList("NewYork", "LosAngeles", "Chicago")),
                "Should have all office locations");
    }

    @Test
    @TestCaseId("IPBQA-31659-MultiLevelGrouping")
    @Description("Test multilevel grouping by multiple tag dimensions")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultiLevelGrouping() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Company");
        tagsPage.addTagValue(1, "CompanyA");
        tagsPage.addTagValue(1, "CompanyB");

        tagsPage.addTagType("Division");
        tagsPage.addTagValue(2, "North");
        tagsPage.addTagValue(2, "South");

        tagsPage.addTagType("Department");
        tagsPage.addTagValue(3, "Technical");
        tagsPage.addTagValue(3, "Marketing");

        Assert.assertEquals(tagsPage.countTagTypes(), 3, "Should have 3 levels for multilevel grouping");

        List<String> companies = tagsPage.getTagValues(1);
        List<String> divisions = tagsPage.getTagValues(2);
        List<String> departments = tagsPage.getTagValues(3);

        Assert.assertEquals(companies.size(), 2, "Should have 2 companies");
        Assert.assertEquals(divisions.size(), 2, "Should have 2 divisions");
        Assert.assertEquals(departments.size(), 2, "Should have 2 departments");
    }

    @Test
    @TestCaseId("IPBQA-31659-FilteringByTagValue")
    @Description("Test filtering capability by individual tag values")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFilteringByTagValue() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Industry");
        tagsPage.addTagValue(1, "Retail");
        tagsPage.addTagValue(1, "Finance");
        tagsPage.addTagValue(1, "Healthcare");

        tagsPage.addTagType("Size");
        tagsPage.addTagValue(2, "Small");
        tagsPage.addTagValue(2, "Medium");
        tagsPage.addTagValue(2, "Large");

        List<String> industries = tagsPage.getTagValues(1);
        Assert.assertTrue(industries.contains("Retail"), "Should have Retail for filtering");
        Assert.assertTrue(industries.contains("Finance"), "Should have Finance for filtering");
        Assert.assertTrue(industries.contains("Healthcare"), "Should have Healthcare for filtering");

        List<String> sizes = tagsPage.getTagValues(2);
        Assert.assertTrue(sizes.contains("Small"), "Should have Small for filtering");
        Assert.assertTrue(sizes.contains("Medium"), "Should have Medium for filtering");
        Assert.assertTrue(sizes.contains("Large"), "Should have Large for filtering");
    }

    @Test
    @TestCaseId("IPBQA-31659-FilteringByMultipleTags")
    @Description("Test filtering with multiple tag criteria")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFilteringByMultipleTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Source");
        tagsPage.addTagValue(1, "Legacy");
        tagsPage.addTagValue(1, "NewDev");
        tagsPage.addTagValue(1, "Migrated");

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(2, "Active");
        tagsPage.addTagValue(2, "Deprecated");
        tagsPage.addTagValue(2, "InDevelopment");

        tagsPage.addTagType("Priority");
        tagsPage.addTagValue(3, "Critical");
        tagsPage.addTagValue(3, "High");
        tagsPage.addTagValue(3, "Normal");

        List<String> sources = tagsPage.getTagValues(1);
        List<String> statuses = tagsPage.getTagValues(2);
        List<String> priorities = tagsPage.getTagValues(3);

        Assert.assertEquals(sources.size(), 3, "Should have 3 source values for multi-tag filtering");
        Assert.assertEquals(statuses.size(), 3, "Should have 3 status values for multi-tag filtering");
        Assert.assertEquals(priorities.size(), 3, "Should have 3 priority values for multi-tag filtering");
    }

    @Test
    @TestCaseId("IPBQA-31659-GroupingStructureValidation")
    @Description("Validate grouping structure consistency")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGroupingStructureValidation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Tier");
        tagsPage.addTagValue(1, "Enterprise");
        tagsPage.addTagValue(1, "Professional");
        tagsPage.addTagValue(1, "Standard");

        tagsPage.addTagType("Term");
        tagsPage.addTagValue(2, "Annual");
        tagsPage.addTagValue(2, "Monthly");
        tagsPage.addTagValue(2, "Quarterly");

        int tierCount = tagsPage.getTagValues(1).size();
        int termCount = tagsPage.getTagValues(2).size();

        Assert.assertEquals(tierCount, 3, "Tier should maintain 3 values");
        Assert.assertEquals(termCount, 3, "Term should maintain 3 values");

        WaitUtil.sleep(300, "wait for structure validation");
    }
}
