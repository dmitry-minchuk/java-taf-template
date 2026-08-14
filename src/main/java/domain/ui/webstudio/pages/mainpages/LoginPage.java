package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.ConfigureCommitInfoComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

public class LoginPage extends BasePage {

    private static final String COMPLETE_PROFILE_MODAL = "xpath=//div[@role='dialog']"
            + "[.//div[contains(@class,'ant-modal-title') and normalize-space()='Complete Your Profile']]";
    private static final String SHELL_READY_SELECTOR = ".ant-modal-wrap, ul.ant-menu-horizontal";
    private static final int PROFILE_SAVE_ATTEMPTS = 3;
    private static final int PROFILE_MODAL_PROBE_MS = DEFAULT_TIMEOUT_MS;
    private static final int PROFILE_MODAL_LATE_PROBE_MS = 3000;
    private static final int MODAL_CLOSED_POLL_MS = 500;

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

    public void completeProfileIfRequested() {
        try {
            page.waitForSelector(SHELL_READY_SELECTOR,
                    new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        } catch (RuntimeException notSignedIn) {
            return;
        }
        for (int attempt = 1; attempt <= PROFILE_SAVE_ATTEMPTS; attempt++) {
            int probeMs = attempt == 1 ? PROFILE_MODAL_PROBE_MS : PROFILE_MODAL_LATE_PROBE_MS;
            if (!completeProfileModal.isVisible(probeMs)) {
                return;
            }
            LOGGER.info("Filling the Complete Your Profile modal, attempt {}", attempt);
            completeProfileComponent.fillCommitInfoWithRandomData();
            WaitUtil.waitForCondition(() -> !completeProfileModal.isVisible(MODAL_CLOSED_POLL_MS),
                    DEFAULT_TIMEOUT_MS, 250, "Waiting for the Complete Your Profile modal to close");
        }
        if (completeProfileModal.isVisible(MODAL_CLOSED_POLL_MS)) {
            throw new IllegalStateException("The Complete Your Profile modal did not close after "
                    + PROFILE_SAVE_ATTEMPTS + " attempts");
        }
    }

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
