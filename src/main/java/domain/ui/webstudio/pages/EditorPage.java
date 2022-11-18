package domain.ui.webstudio.pages;

import domain.ui.BasePage;
import domain.ui.webstudio.components.CurrentUserComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EditorPage extends BasePage {
    public CurrentUserComponent currentUserComponent = new CurrentUserComponent(driver, By.cssSelector("#rb > span"));

    public EditorPage(WebDriver driver) {
        super(driver, "/");
    }
}
