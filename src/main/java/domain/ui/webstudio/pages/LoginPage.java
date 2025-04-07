package domain.ui.webstudio.pages;

import configuration.core.SmartWebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.BasePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    @FindBy(id = "loginName")
    private SmartWebElement loginTextField;

    @FindBy(id = "loginPassword")
    private SmartWebElement passwordTextField;

    @FindBy(id = "loginSubmit")
    private SmartWebElement signInBtn;

    public LoginPage() {
        super("/");
    }

    public EditorPage login(UserData user) {
        loginTextField.sendKeys(user.getLogin());
        passwordTextField.sendKeys(user.getPassword());
        signInBtn.click();
        return new EditorPage();
    }
}
