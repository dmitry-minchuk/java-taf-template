package web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import web.components.Deals;
import web.components.Header;

public class AmazonDealsPage extends BasePage{

    @FindBy(xpath = "//label[span[contains(text(), 'Cell Phones')]]//input[@type='checkbox']")
    private WebElement cellPhonesCheckbox;

    @FindBy(xpath = "//ol[@class='a-carousel']//span[contains(text(), 'All Deals')]")
    private WebElement allDealsCarouselBtn;

    public Deals deals = new Deals(getDriver(), By.xpath("//div[contains(@class, 'dealContainer')]"));
    public Header header = new Header(getDriver(), By.xpath("//header"));

    public AmazonDealsPage() {
        super();
    }

    public boolean isPageOpened() {
        return allDealsCarouselBtn.isDisplayed();
    }

    public void filterElementsAndValidateAmount() {
        cellPhonesCheckbox.click();
        Assert.assertFalse(deals.getElementsCollection().isEmpty(), "No elements found!");
    }

    public String getContainerProductName(int num) {
        return deals.getElementsTitle(num);
    }
}
