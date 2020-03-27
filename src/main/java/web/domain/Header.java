package web.domain;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$x;

public class Header {
    private String path = "";
    private SelenideElement siteSearchSelector = $x(path + "//form[@name='site-search']//select");
    private SelenideElement siteSearchField = $x(path + "//form[@name='site-search']//input[@type='text']");
    private SelenideElement siteSearchBtn = $x(path + "//form[@name='site-search']//input[@type='submit']");

    public Header(String xpathSelector) {
        path = xpathSelector;
    }

    public void selectSearchArea(String searchArea, String searchQuery) {
        siteSearchSelector.selectOption(searchArea);
        siteSearchField.setValue(searchQuery);
        siteSearchBtn.click();
    }
}
