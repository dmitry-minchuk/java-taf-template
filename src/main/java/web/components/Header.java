package web.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class Header {
    private WebDriver driver;
    private WebElement selfElement;

    public Header(WebDriver driver, By by) {
        this.driver = driver;
        selfElement = this.driver.findElement(by);
    }

    public void selectSearchArea(String searchArea, String searchQuery) {
        getSiteSearchSelector().getWrappedElement().click();
        getSiteSearchSelector().selectByVisibleText(searchArea);
        getSiteSearchField().sendKeys(searchQuery);
        getSiteSearchBtn().click();
    }

    public Select getSiteSearchSelector() {
        WebElement searchSelectorElement = selfElement.findElement(By.xpath(".//form[@name='site-search']//select"));
        return new Select(searchSelectorElement);
    }

    public WebElement getSiteSearchField() {
        return selfElement.findElement(By.xpath(".//form[@name='site-search']//input[@type='text']"));
    }

    public WebElement getSiteSearchBtn() {
        return selfElement.findElement(By.xpath(".//form[@name='site-search']//input[@type='submit']"));
    }
}
