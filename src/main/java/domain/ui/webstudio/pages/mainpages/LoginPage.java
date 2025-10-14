package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

public class LoginPage extends BasePage {

    private WebElement usernameField;
    private WebElement passwordField;
    private WebElement loginButton;
    private WebElement loginErrorMessage;

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
        loginErrorMessage = new WebElement(page, "xpath=//*[@class='error' or @id='input-error']", "loginErrorMessage");
    }

    public EditorPage login(UserData user) {
        return login(user, DEFAULT_TIMEOUT_MS);
    }

    public EditorPage login(UserData user, long EXTENDED_TIME_PERIOD) {
        WaitUtil.waitForCondition(() -> page.url().contains("/login"), EXTENDED_TIME_PERIOD, 250);
        usernameField.waitForVisible(EXTENDED_TIME_PERIOD);
        usernameField.fill(user.getLogin());
        passwordField.fill(user.getPassword());
        loginButton.click();
        return new EditorPage();
    }

    public String getLoginErrorMessage() {
        return loginErrorMessage.waitForVisible(DEFAULT_TIMEOUT_MS).getText();
    }

    public boolean isLoginErrorDisplayed() {
        return loginErrorMessage.isVisible();
    }
}
