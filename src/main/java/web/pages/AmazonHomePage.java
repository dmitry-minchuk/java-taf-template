package web.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

public class AmazonHomePage extends BasePage {

    private SelenideElement todaysDealsLink = $(By.xpath("//div[@id='nav-xshop']//a[contains(text(), 'Deals')]"));

    public AmazonDealsPage clickDealsLink() {
        todaysDealsLink.click();
        return new AmazonDealsPage();
    }
}
