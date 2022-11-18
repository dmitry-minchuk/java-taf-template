package domain.ui.webstudio.pages.mainpages;

import org.openqa.selenium.WebDriver;

public class RepositoryPage extends ProxyMainPage {

    public RepositoryPage(WebDriver driver) {
        super(driver, "/faces/pages/modules/repository/index.xhtml");
    }
}
