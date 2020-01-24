package web.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Selenide.$;

public class GoogleHomePage extends BasePage {

    @FindBy(css = "input[name=q]")
    private SelenideElement searchField;

    public GoogleHomePage() {
        super();
    }

    public GoogleHomePage(String urlAppender) {
        super(urlAppender);
    }

    public void search(String text) {
        $(By.cssSelector("input[name=q]")).setValue(text).pressEnter();
        searchField.setValue(text).pressEnter();
    }
}
