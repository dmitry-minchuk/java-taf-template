package domain.ui.webstudio.pages;

import domain.servicemodels.UserData;
import domain.ui.BasePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
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

    public EditorPage login(UserData user) {
        loginTextField.sendKeys(user.getLogin());
        passwordTextField.sendKeys(user.getPassword());
        signInBtn.click();
        return new EditorPage(driver);
    }
}
