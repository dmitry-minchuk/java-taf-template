package web.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleHomePage extends BasePage {

    @FindBy(css = "input[name=q]")
    private WebElement searchField;

    public GoogleHomePage() {
        super();
    }

    public GoogleHomePage(String urlAppender) {
        super(urlAppender);
    }

    public void search(String text) {
        searchField.sendKeys(text);
        searchField.sendKeys(Keys.ENTER);
    }
}
