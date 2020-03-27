package web.components;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$x;

public class Header {
    private SelenideElement selfElement;

    public Header(String xpathSelector) {
        selfElement = $x(xpathSelector);
    }

    public void selectSearchArea(String searchArea, String searchQuery) {
        getSiteSearchSelector().selectOption(searchArea);
        getSiteSearchField().setValue(searchQuery);
        getSiteSearchBtn().click();
    }

    public SelenideElement getSiteSearchSelector() {
        return selfElement.$x(".//form[@name='site-search']//select");
    }

    public SelenideElement getSiteSearchField() {
        return selfElement.$x(".//form[@name='site-search']//input[@type='text']");
    }

    public SelenideElement getSiteSearchBtn() {
        return selfElement.$x(".//form[@name='site-search']//input[@type='submit']");
    }
}
