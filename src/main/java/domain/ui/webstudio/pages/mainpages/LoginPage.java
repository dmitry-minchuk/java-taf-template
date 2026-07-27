package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.ConfigureCommitInfoComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

public class LoginPage extends BasePage {

    // Studio 6.4.0 raises a mandatory "Complete Your Profile" modal on the first login of every user;
    // it blocks the whole shell (its overlay swallows top-nav clicks) until the profile is saved.
    private static final String COMPLETE_PROFILE_MODAL = "xpath=//div[@role='dialog']"
            + "[.//div[contains(@class,'ant-modal-title') and normalize-space()='Complete Your Profile']]";
    private static final String SHELL_READY_SELECTOR = ".ant-modal-wrap, ul.ant-menu-horizontal";

    private WebElement usernameField;
    private WebElement passwordField;
    private WebElement loginButton;
    private WebElement loginErrorMessage;
    private WebElement completeProfileModal;
    private ConfigureCommitInfoComponent completeProfileComponent;

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
        loginErrorMessage = new WebElement(page, "xpath=//div[contains(@class,'ant-alert-error')]", "loginErrorMessage");
        completeProfileModal = new WebElement(page, COMPLETE_PROFILE_MODAL, "completeProfileModal");
        completeProfileComponent = createScopedComponent(ConfigureCommitInfoComponent.class, COMPLETE_PROFILE_MODAL, "completeProfileComponent");
    }

    public EditorPage login(UserData user) {
        return login(user, 15000);
    }

    public EditorPage login(UserData user, long EXTENDED_TIME_PERIOD) {
        usernameField.waitForVisible(EXTENDED_TIME_PERIOD);
        usernameField.fill(user.getLogin());
        passwordField.fill(user.getPassword());
        loginButton.click();
        completeProfileIfRequested();
        return new EditorPage();
    }

    /**
     * Fills the 6.4.0 "Complete Your Profile" modal when the logged-in user has no profile yet.
     * Waits for either the modal or the loaded shell so a returning user costs no extra timeout.
     */
    public void completeProfileIfRequested() {
        page.waitForSelector(SHELL_READY_SELECTOR,
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        if (completeProfileModal.isVisible(DEFAULT_TIMEOUT_MS / 10)) {
            completeProfileComponent.fillCommitInfoWithRandomData();
            completeProfileModal.waitForHidden(DEFAULT_TIMEOUT_MS);
        }
    }

    /** True if the Studio login form is shown within the timeout (e.g. after sign-out). */
    public boolean isLoginFormDisplayed(int timeoutInMillis) {
        return usernameField.isVisible(timeoutInMillis);
    }

    public String getLoginErrorMessage() {
        return loginErrorMessage.waitForVisible(DEFAULT_TIMEOUT_MS).getText();
    }

    public boolean isLoginErrorDisplayed() {
        return loginErrorMessage.isVisible();
    }

    public boolean isLoginErrorDisplayed(int timeoutInMillis) {
        return loginErrorMessage.isVisible(timeoutInMillis);
    }
}
