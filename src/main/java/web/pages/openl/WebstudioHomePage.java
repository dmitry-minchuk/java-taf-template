package web.pages.openl;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import web.pages.BasePage;

import static com.codeborne.selenide.Selenide.$;

public class WebstudioHomePage extends BasePage {

    private SelenideElement nameTesxField = $(By.cssSelector("input[name=username]"));
    private SelenideElement passTesxField = $(By.cssSelector("input[name=password]"));
    private SelenideElement loginBtn = $(By.cssSelector("input[id=loginSubmit]"));

    public WebstudioHomePage() {
        super();
    }

    public WebstudioHomePage(String urlAppender) {
        super(urlAppender);
    }

    public void login(String login, String pass) {
        nameTesxField.setValue(login);
        passTesxField.setValue(pass);
        loginBtn.click();
    }
}
