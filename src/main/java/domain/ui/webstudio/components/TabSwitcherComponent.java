package domain.ui.webstudio.components;

import domain.ui.BasePage;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TabSwitcherComponent {
    private WebDriver driver;
    private WebElement selfElement;

    public TabSwitcherComponent(WebDriver driver, By by) {
        this.driver = driver;
        selfElement = this.driver.findElement(by);
    }

    public BasePage selectTab(TabName tabName) {
        String tabLocator = "./a[text()='%s']";
        selfElement.findElement(By.xpath(String.format(tabLocator, tabName.getValue()))).click();

        if(tabName.equals(TabName.EDITOR)) {
            return new EditorPage(driver);
        } else if(tabName.equals(TabName.REPOSITORY)) {
            return new RepositoryPage(driver);
        } else
            return new AdminPage(driver);
    }

    public static enum TabName {
        EDITOR("EDITOR"),
        REPOSITORY("REPOSITORY"),
        ADMIN("ADMIN");

        private String value;

        TabName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
