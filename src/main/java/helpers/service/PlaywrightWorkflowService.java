package helpers.service;

import configuration.driver.PlaywrightDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import domain.ui.webstudio.pages.mainpages.PlaywrightRepositoryPage;
import helpers.utils.StringUtil;

/**
 * Playwright version of WorkflowService providing complete login → project creation → editor workflow
 * Supports both LOCAL and DOCKER execution modes with automatic delegation
 */
public class PlaywrightWorkflowService {
    public static String loginCreateProjectOpenEditor(User user, CreateNewProjectComponent.TabName projectType, String sourceName) {
        PlaywrightLoginService loginService = new PlaywrightLoginService(PlaywrightDriverPool.getPage());
        PlaywrightEditorPage editorPage = loginService.login(UserService.getUser(user));
        PlaywrightRepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProject(projectType, projectName, sourceName);
        repositoryPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.EDITOR);
        return projectName;
    }

    public static String loginCreateProjectFromZipOpenEditor(User user, String zipFileName) {
        PlaywrightLoginService loginService = new PlaywrightLoginService(PlaywrightDriverPool.getPage());
        PlaywrightEditorPage editorPage = loginService.login(UserService.getUser(user));
        PlaywrightRepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, zipFileName);
        repositoryPage.getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.EDITOR);
        return projectName;
    }
}