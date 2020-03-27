package web.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import web.components.Header;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class AmazonDealsPage extends BasePage{

    private SelenideElement dealsHeader = $x("//h1//b[contains(text(), 'Deals')]");
    private SelenideElement cellPhonesCheckbox = $x("//label[span[contains(text(), 'Cell Phones')]]//input[@type='checkbox']");
    private ElementsCollection dealContainers = $$x("//div[contains(@class, 'dealContainer')]");
    public Header header = new Header("//header");

    public boolean isPageOpened() {
        return dealsHeader.isDisplayed();
    }

    public void filterElements() {
        cellPhonesCheckbox.click();
        dealContainers.shouldHave(CollectionCondition.sizeGreaterThan(0));
    }

    public String getContainerProductName(int num) {
        return dealContainers.get(num).find(By.xpath(".//a[@id='dealTitle']")).getText();
    }
}
