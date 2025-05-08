package domain.ui.webstudio.components;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class CurrentUserComponent extends BasePageComponent {

    @FindBy(xpath = "./a")
    private SmartWebElement dropdownToggle;

    @FindBy(xpath = ".//li/a[contains(text(), '%s')]")
    private SmartWebElement menuOption;

    public CurrentUserComponent() {}

    public void openDropdownMenuAndSelect(MenuElements element) {
        dropdownToggle.click();
        menuOption.format(element.getValue()).click();
        getDriver().switchTo().alert().accept();
    }

    @Getter
    public enum MenuElements {
        USER_DETAILS("User details"),
        USER_SETTINGS("User settings"),
        HELP("Help"),
        SIGN_OUT("Sign out");

        private String value;

        MenuElements(String value) {
            this.value = value;
        }
    }
}
