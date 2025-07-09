package domain.ui.webstudio.components;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class CurrentUserComponent extends BasePageComponent {

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='%s']]")
    private SmartWebElement menuOption;

    public CurrentUserComponent() {
        super();
        WaitUtil.sleep(1);
    }

    public void select(MenuElements element) {
        menuOption.format(element.getValue()).click();
    }

    public AdminPage navigateToAdministration() {
        select(MenuElements.ADMINISTRATION);
        return new AdminPage();
    }

    @Getter
    public enum MenuElements {
        MY_PROFILE("My Profile"),
        MY_SETTINGS("My Settings"),
        ADMINISTRATION("Administration"),
        HELP("Help"),
        SIGN_OUT("Sign Out");

        private String value;

        MenuElements(String value) {
            this.value = value;
        }
    }
}
