package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.BasePage;

public class LoginPage extends BasePage {

    private WebElement usernameField;
    private WebElement passwordField;
    private WebElement loginButton;

    public LoginPage() {
        super();
        initializeComponents();
    }

    public LoginPage(Page page) {
        super(page);
        initializeComponents();
    }

    private void initializeComponents() {
        usernameField = new WebElement(page, "xpath=//input[@id='username']", "usernameField");
        passwordField = new WebElement(page, "xpath=//input[@id='password']", "passwordField");
        loginButton = new WebElement(page, "xpath=//button[@type='submit']", "loginButton");
    }

    public EditorPage login(UserData user) {
        return login(user, DEFAULT_TIMEOUT_MS);
    }

    public EditorPage login(UserData user, long EXTENDED_TIME_PERIOD) {
        usernameField.waitForVisible(EXTENDED_TIME_PERIOD);
        usernameField.fill(user.getLogin());
        passwordField.fill(user.getPassword());
        loginButton.click();
        return new EditorPage();
    }
}
