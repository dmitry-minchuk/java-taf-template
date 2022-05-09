package web.pages.amazon;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import web.components.Deals;
import web.components.Header;
import web.pages.BasePage;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class AmazonDealsPage extends BasePage {

    private SelenideElement dealsHeader = $x("//h1//b[contains(text(), 'Deals')]");
    private SelenideElement cellPhonesCheckbox = $x("//label[span[contains(text(), 'Cell Phones')]]//input[@type='checkbox']");
    public Deals deals = new Deals("//div[contains(@class, 'dealContainer')]");
    public Header header = new Header("//header");

    public boolean isPageOpened() {
        return dealsHeader.isDisplayed();
    }

    public void filterElements() {
        cellPhonesCheckbox.click();
        deals.getElementsCollection().shouldHave(CollectionCondition.sizeGreaterThan(0));
    }

    public String getContainerProductName(int num) {
        return deals.getElementsTitle(num);
    }
}
