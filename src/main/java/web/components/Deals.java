package web.components;

import com.codeborne.selenide.ElementsCollection;

import static com.codeborne.selenide.Selenide.$$x;

public class Deals {
    private ElementsCollection dealList;

    public Deals(String xpathSelector) {
        dealList = $$x(xpathSelector);
    }

    public String getElementsTitle(int index) {
        return dealList.get(index).$x(".//a[@id='dealTitle']").getText();
    }

    public ElementsCollection getElementsCollection() {
        return dealList;
    }
}
