package web.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

public class GoogleHomePage extends BasePage {

    private SelenideElement searchField = $(By.cssSelector("input[name=q]"));

    public GoogleHomePage() {
        super();
    }

    public GoogleHomePage(String urlAppender) {
        super(urlAppender);
    }

    public void search(String text) {
        searchField.setValue(text).pressEnter();
    }
}
