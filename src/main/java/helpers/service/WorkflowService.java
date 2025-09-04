package helpers.service;

import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.PlaywrightCreateNewProjectComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.StringUtil;

// Playwright version of WorkflowService for complete login → project creation → editor workflow
public class WorkflowService {
    private static String loginCreateProject(User user, PlaywrightCreateNewProjectComponent.TabName projectType, String sourceName) {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(user));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProject(projectType, projectName, sourceName);
        repositoryPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.EDITOR);
        return projectName;
    }

    public static String loginCreateProjectFromZip(User user, String zipFileName) {
        return loginCreateProject(user, PlaywrightCreateNewProjectComponent.TabName.ZIP_ARCHIVE, zipFileName);
    }

    public static String loginCreateProjectFromExcelFile(User user, String excelFileName) {
        return loginCreateProject(user, PlaywrightCreateNewProjectComponent.TabName.EXCEL_FILES, excelFileName);
    }

    public static String loginCreateProjectFromTemplate(User user, String templateName) {
        return loginCreateProject(user, PlaywrightCreateNewProjectComponent.TabName.TEMPLATE, templateName);
    }
}