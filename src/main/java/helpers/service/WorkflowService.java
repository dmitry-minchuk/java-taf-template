package helpers.service;

import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.StringUtil;

public class WorkflowService {

    public static String loginCreateProjectOpenEditor(User user, CreateNewProjectComponent.TabName projectType, String sourceName) {
        EditorPage editorPage = new LoginService().login(UserService.getUser(user));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String projectName = StringUtil.generateUniqueName("project");
        repositoryPage.createProject(projectType, projectName, sourceName);
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        return projectName;
    }
}
