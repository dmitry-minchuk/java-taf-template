package domain.ui.webstudio.components;

import configuration.driver.DriverPool;
import domain.ui.BasePage;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TabSwitcherComponent {
    private WebElement selfElement;

    public TabSwitcherComponent(By by) {
        selfElement = DriverPool.getDriver().findElement(by);
    }

    public BasePage selectTab(TabName tabName) {
        String tabLocator = "./a[text()='%s']";
        selfElement.findElement(By.xpath(String.format(tabLocator, tabName.getValue()))).click();

        if(tabName.equals(TabName.EDITOR)) {
            return new EditorPage();
        } else if(tabName.equals(TabName.REPOSITORY)) {
            return new RepositoryPage();
        } else
            return new AdminPage();
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
