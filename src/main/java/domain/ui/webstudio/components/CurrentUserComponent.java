package domain.ui.webstudio.components;

import configuration.driver.DriverPool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CurrentUserComponent {
    private WebElement selfElement;

    public CurrentUserComponent(By by) {
        selfElement = DriverPool.getDriver().findElement(by);
    }

    public void openDropdownMenuAndSelect(MenuElements element) {
        selfElement.findElement(By.xpath("./a")).click();
        String menuElementLocator = String.format(".//li/a[contains(text(), '%s')]", element.getValue());
        selfElement.findElement(By.xpath(menuElementLocator)).click();
        DriverPool.getDriver().switchTo().alert().accept();
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
