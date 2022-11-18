package domain.ui.webstudio.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CurrentUserComponent {
    private WebDriver driver;
    private WebElement selfElement;

    public CurrentUserComponent(WebDriver driver, By by) {
        this.driver = driver;
        selfElement = this.driver.findElement(by);
    }

    public void openDropdownMenuAndSelect(MenuElements element) {
        selfElement.findElement(By.xpath("./a")).click();
        String menuElementLocator = String.format(".//li/a[contains(text(), '%s')]", element.getValue());
        selfElement.findElement(By.xpath(menuElementLocator)).click();
        driver.switchTo().alert().accept();
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
