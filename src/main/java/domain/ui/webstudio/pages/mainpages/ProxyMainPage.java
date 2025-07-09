package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.BasePage;
import configuration.core.ui.SmartWebElement;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.components.TabSwitcherComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public abstract class ProxyMainPage extends BasePage {

    @FindBy(xpath = "//div[@class='ant-drawer-content-wrapper']")
    private CurrentUserComponent currentUserComponent;

    @FindBy(xpath = "//div[@class='user-logo']/span")
    private SmartWebElement userLogo;

    @Getter
    @FindBy(xpath = "//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]")
    private TabSwitcherComponent tabSwitcherComponent;

    @FindBy(xpath = "//div[@class='message closable']")
    private SmartWebElement message;

    public ProxyMainPage(String urlAppender) {
        super(urlAppender);
    }

    public String getStudioMessage() {
        if(message.isDisplayed())
            return message.getText();
        else
            return null;
    }

    public boolean isStudioMessageDisplayed(String text) {
        return message.isDisplayed() && message.getText().contains(text);
    }

    public CurrentUserComponent getCurrentUserComponent() {
        userLogo.click();
        currentUserComponent.isPresent();
        return currentUserComponent;
    }

}
