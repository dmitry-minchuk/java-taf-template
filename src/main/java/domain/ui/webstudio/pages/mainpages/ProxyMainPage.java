package domain.ui.webstudio.pages.mainpages;

import domain.ui.BasePage;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class ProxyMainPage extends BasePage {
    public CurrentUserComponent currentUserComponent = new CurrentUserComponent(driver, By.cssSelector("#rb > span"));
    public TabSwitcherComponent tabSwitcherComponent = new TabSwitcherComponent(driver, By.xpath("//div/div[@id='ll']"));


    public ProxyMainPage(WebDriver driver) {
        super(driver);
    }

    public ProxyMainPage(WebDriver driver, String urlAppender) {
        super(driver, urlAppender);
    }
}
