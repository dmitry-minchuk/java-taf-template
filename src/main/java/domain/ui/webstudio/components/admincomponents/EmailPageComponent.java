package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class EmailPageComponent extends BaseComponent {

    private static final String SAVED_MESSAGE = "Email server configuration saved";
    private static final int APPLY_OUTCOME_TIMEOUT_MS = 10_000;

    private WebElement emailVerificationCheckbox;
    private WebElement emailUrlField;
    private WebElement emailUsernameField;
    private WebElement emailPasswordField;
    private WebElement applyBtn;
    private WebElement showPasswordBtn;
    private WebElement loginUsernameField;

    public EmailPageComponent() {
        super(DriverPool.getPage());
        initializeEmailComponents();
    }
    
    public EmailPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeEmailComponents();
    }

    private void initializeEmailComponents() {
        emailVerificationCheckbox = createScopedElement("xpath=.//input[@type='checkbox']", "Email Verification Checkbox");
        emailUrlField = createScopedElement("xpath=.//div[./div/label[@title='URL']]//div/input", "Email URL Field");
        emailUsernameField = createScopedElement("xpath=.//div[./div/label[@title='Username']]//div/input", "Email Username Field");
        emailPasswordField = createScopedElement("xpath=.//input[@id='password']", "Email Password Field");
        applyBtn = createScopedElement("xpath=.//button[./span[text()='Apply']]", "Apply Button");
        showPasswordBtn = createScopedElement("xpath=.//span[contains(@aria-label,'eye')]", "Show Password Button");
        loginUsernameField = new WebElement(page, "xpath=//input[@id='username']", "Login Username Field");
    }

    public void enableEmailVerification() {
        if (!isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    public void disableEmailVerification() {
        if (isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    public boolean isEmailVerificationEnabled() {
        return emailUrlField.isVisible();
    }

    public void setEmailUrl(String url) {
        emailUrlField.clear();
        emailUrlField.fill(url);
    }

    public void setEmailUsername(String username) {
        emailUsernameField.clear();
        emailUsernameField.fill(username);
    }

    public void setEmailPassword(String password) {
        emailPasswordField.clear();
        emailPasswordField.fill(password);
    }

    public String getEmailUrl() {
        return emailUrlField.getAttribute("value");
    }

    public String getEmailUsername() {
        return emailUsernameField.getAttribute("value");
    }

    public String getEmailPassword() {
        return Objects.requireNonNullElse(emailPasswordField.getAttribute("value"), "");
    }

    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);
        applyBtn.click();
        getModalOkBtn().click();
        Set<String> popupMessages = new LinkedHashSet<>();
        long deadline = System.currentTimeMillis() + APPLY_OUTCOME_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            popupMessages.addAll(getAllMessagesFullText());
            boolean savedMessageShown = popupMessages.stream().anyMatch(m -> m.contains(SAVED_MESSAGE));
            if (savedMessageShown || loginUsernameField.isVisible(200)) {
                LOGGER.info("Email settings applied; popup messages seen: {}", popupMessages);
                return;
            }
        }
        throw new AssertionError("Applying the email settings reported neither success nor logout; popup messages seen: "
                + popupMessages);
    }

    public void enableEmailVerificationWithCredentials(String url, String username, String password) {
        enableEmailVerification();
        setEmailCredentials(url, username, password);
    }

    public void togglePasswordVisibility() {
        showPasswordBtn.click();
    }

    public String getPasswordFieldType() {
        return emailPasswordField.getAttribute("type");
    }

    public boolean isPasswordVisible() {
        String type = getPasswordFieldType();
        return type != null && !type.equals("password");
    }
}