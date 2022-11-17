package web.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleHomePage extends BasePage {

    @FindBy(css = "input[name=q]")
    private WebElement searchField;

    public GoogleHomePage(WebDriver driver) {
        super(driver);
    }

    public GoogleHomePage(WebDriver driver, String urlAppender) {
        super(driver, urlAppender);
    }

    public void search(String text) {
        searchField.sendKeys(text);
        searchField.sendKeys(Keys.ENTER);
    }
}
