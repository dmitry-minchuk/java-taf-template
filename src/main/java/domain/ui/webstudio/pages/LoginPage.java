package domain.ui.webstudio.pages;

import domain.ui.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    @FindBy(id = "loginName")
    private WebElement loginTextField;

    @FindBy(id = "loginPassword")
    private WebElement passwordTextField;

    @FindBy(id = "loginSubmit")
    private WebElement signInBtn;

    public LoginPage(WebDriver driver) {
        super(driver, "/");
    }

    public EditorPage login(String login, String password) {
        loginTextField.sendKeys(login);
        passwordTextField.sendKeys(password);
        signInBtn.click();
        return new EditorPage(driver);
    }
}
