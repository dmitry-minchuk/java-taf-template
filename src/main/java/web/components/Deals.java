package web.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Deals {
    private WebDriver driver;
    private List<WebElement> selfElementList;

    public Deals(WebDriver driver, By by) {
        this.driver = driver;
        selfElementList = this.driver.findElements(by);
    }

    public String getElementsTitle(int index) {
        int i = 1;
        for(WebElement deal: selfElementList) {
            if(i == index)
                return deal.findElement(By.xpath(".//a[@id='dealTitle']")).getText();
            i++;
        }
        return null;
    }

    public List<WebElement> getElementsCollection() {
        return selfElementList;
    }
}
