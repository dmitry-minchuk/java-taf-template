package tests.ui.webstudio;

import domain.serviceclasses.constants.ContainerType;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.ContainerService;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testcontainers.containers.GenericContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tests.ContainerizedBaseTest;

public class SampleTest5 extends ContainerizedBaseTest {
    GenericContainer<?> webstudio;

    @BeforeClass
    public void startContainer() {
        webstudio = ContainerService.createContainer(ContainerType.WEBSTUDIO);
    }

    @AfterClass
    public void stopContainer() {
        webstudio.stop();
    }

    @Test
    public void test() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }
}
