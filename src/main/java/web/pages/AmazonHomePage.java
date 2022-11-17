package web.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.Waiter;

public class AmazonHomePage extends BasePage {

    @FindBy(xpath = "//div[@id='nav-xshop']//a[contains(text(), 'Deals')]")
    private WebElement todaysDealsLink;

    @FindBy(xpath = "//input[@data-action-type='DISMISS' and following-sibling::span[contains(text(), \"Don't Change\")]]")
    private WebElement locationPopupDismissBtn;

    public AmazonHomePage(WebDriver driver) {
        super(driver);
    }

    public AmazonDealsPage clickDealsLink() {
        if(Waiter.waitUntil(driver, ExpectedConditions.visibilityOf(locationPopupDismissBtn), 2))
            locationPopupDismissBtn.click();
        todaysDealsLink.click();
        return new AmazonDealsPage(driver);
    }
}
