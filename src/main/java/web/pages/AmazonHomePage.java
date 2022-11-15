package web.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AmazonHomePage extends BasePage {

    @FindBy(xpath = "//div[@id='nav-xshop']//a[contains(text(), 'Deals')]")
    private WebElement todaysDealsLink;

    @FindBy(xpath = "//input[@data-action-type='DISMISS' and following-sibling::span[contains(text(), \"Don't Change\")]]")
    private WebElement locationPopupDismissBtn;

    public AmazonHomePage() {
        super();
    }

    public AmazonDealsPage clickDealsLink() {
        locationPopupDismissBtn.click();
        todaysDealsLink.click();
        return new AmazonDealsPage();
    }
}
