package web.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AmazonHomePage extends BasePage {

    @FindBy(xpath = "//div[@id='nav-xshop']//a[contains(text(), 'Deals')]")
    private WebElement todaysDealsLink;

    public AmazonHomePage() {
        super();
    }

    public AmazonDealsPage clickDealsLink() {
        todaysDealsLink.click();
        return new AmazonDealsPage();
    }
}
