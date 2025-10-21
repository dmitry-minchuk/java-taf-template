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

public class TestTagsAutoFill extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-TemplateSetup")
    @Description("Setup project name templates for automatic tag extraction")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectNameTemplateSetup() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");

        tagsPage.setProjectNameTemplates("*-%Domain%-%LOB%\n*+%Domain%+%LOB%\n*.%Domain%.%LOB%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to save");

        String templateError = tagsPage.getTemplateError();
        Assert.assertTrue(templateError.isEmpty(), "Template should be saved without errors");

        String savedTemplates = tagsPage.getProjectNameTemplates();
        Assert.assertTrue(savedTemplates.contains("%Domain%") && savedTemplates.contains("%LOB%"),
                "Templates should contain tag placeholders");
    }

    @Test
    @TestCaseId("IPBQA-31659-TemplateValidation")
    @Description("Validate template syntax and error handling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTemplateValidation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Category");
        tagsPage.addTagValue(1, "Premium");
        tagsPage.addTagValue(1, "Standard");

        tagsPage.setProjectNameTemplates("*-%Category%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for templates to save");

        String error = tagsPage.getTemplateError();
        Assert.assertTrue(error.isEmpty(), "Valid template should not produce error");
    }

    @Test
    @TestCaseId("IPBQA-31659-TemplateClear")
    @Description("Clear project name templates")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTemplateClear() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Type");
        tagsPage.addTagValue(1, "Standard");

        tagsPage.setProjectNameTemplates("*-%Type%");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(300, "wait for templates to save");

        tagsPage.setProjectNameTemplates("");
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(300, "wait for templates to clear");

        String clearedTemplates = tagsPage.getProjectNameTemplates();
        Assert.assertTrue(clearedTemplates.isEmpty(), "Templates should be cleared");
    }

    @Test
    @TestCaseId("IPBQA-31659-MultilineTemplates")
    @Description("Test multiple template patterns in one setup")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultilineTemplates() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(1, "North");
        tagsPage.addTagValue(1, "South");

        tagsPage.addTagType("Environment");
        tagsPage.addTagValue(2, "Production");
        tagsPage.addTagValue(2, "Staging");

        String multilineTemplate = "*-%Region%-%Environment%\n" +
                "*+%Region%+%Environment%\n" +
                "*.%Region%.%Environment%";
        tagsPage.setProjectNameTemplates(multilineTemplate);
        tagsPage.saveProjectNameTemplates();
        WaitUtil.sleep(500, "wait for multiline templates to save");

        String error = tagsPage.getTemplateError();
        Assert.assertTrue(error.isEmpty(), "Multiline template should be valid");

        String saved = tagsPage.getProjectNameTemplates();
        Assert.assertTrue(saved.contains("\n"), "Multiline template should preserve newlines");
    }
}
