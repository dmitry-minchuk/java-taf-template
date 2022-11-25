package domain.ui.webstudio.pages.mainpages;

import domain.ui.BasePage;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import org.openqa.selenium.By;

public abstract class ProxyMainPage extends BasePage {
    public CurrentUserComponent currentUserComponent = new CurrentUserComponent(By.cssSelector("#rb > span"));
    public TabSwitcherComponent tabSwitcherComponent = new TabSwitcherComponent(By.xpath("//div/div[@id='ll']"));

    public ProxyMainPage(String urlAppender) {
        super(urlAppender);
    }
}
