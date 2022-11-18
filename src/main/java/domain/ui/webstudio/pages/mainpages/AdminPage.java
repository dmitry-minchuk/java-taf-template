package domain.ui.webstudio.pages.mainpages;

import org.openqa.selenium.WebDriver;

public class AdminPage extends ProxyMainPage {

    public AdminPage(WebDriver driver) {
        super(driver, "/faces/pages/modules/administration/settings/index.xhtml");
    }
}
