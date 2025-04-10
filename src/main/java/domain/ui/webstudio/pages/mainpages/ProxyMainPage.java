package domain.ui.webstudio.pages.mainpages;

import configuration.core.SmartElementFactory;
import configuration.driver.DriverPool;
import domain.ui.BasePage;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

@Getter
public abstract class ProxyMainPage extends BasePage {

    @FindBy(css = "#rb > span")
    private CurrentUserComponent currentUserComponent;

    @FindBy(xpath = "//div/div[@id='ll']")
    private TabSwitcherComponent tabSwitcherComponent;

    public ProxyMainPage(String urlAppender) {
        super(urlAppender);
        SmartElementFactory.initElements(DriverPool.getDriver(), this);
    }

}
