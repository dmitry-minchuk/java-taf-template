package domain.ui.webstudio.components;

import configuration.core.SmartWebElement;
import configuration.driver.DriverPool;
import domain.ui.BasePageComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    public static enum MenuElements {
        USER_DETAILS("User details"),
        USER_SETTINGS("User settings"),
        HELP("Help"),
        SIGN_OUT("Sign out");

        private String value;

        MenuElements(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
