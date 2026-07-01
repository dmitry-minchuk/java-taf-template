package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

/**
 * The Keycloak login form the browser lands on after Studio (in oauth2 mode) redirects
 * to the external IdP. Used only by PLAYWRIGHT_DOCKER SSO tests where the browser shares
 * the Docker network with Keycloak.
 */
public class KeycloakLoginPage extends BasePage {

    private WebElement usernameField;
    private WebElement passwordField;
    private WebElement loginButton;

    public KeycloakLoginPage() {
        super();
        initializeElements();
    }

    private void initializeElements() {
        usernameField = new WebElement(page, "xpath=//input[@id='username']", "kcUsername");
        passwordField = new WebElement(page, "xpath=//input[@id='password']", "kcPassword");
        loginButton = new WebElement(page, "xpath=//*[@id='kc-login' or (@type='submit' and @name='login')]", "kcLogin");
    }

    public void waitForLoginForm() {
        usernameField.waitForVisible();
    }

    /** True if the IdP login form is shown within the timeout (no live SSO session to re-authenticate). */
    public boolean isLoginFormDisplayed(int timeoutInMillis) {
        return usernameField.isVisible(timeoutInMillis);
    }

    public EditorPage login(String username, String password) {
        waitForLoginForm();
        usernameField.fillSequentially(username);
        passwordField.fillSequentially(password);
        loginButton.click();
        // Wait until the browser is back on Studio (SAML uses an extra POST-binding auto-submit,
        // so the session is only established once we leave the IdP) and the Studio session cookie
        // is set — REST setup that reuses the browser session depends on it.
        Page page = LocalDriverPool.getPage();
        WaitUtil.waitForCondition(() -> {
            page.waitForLoadState();
            String url = page.url();
            boolean onStudio = url != null && !url.contains("/realms/") && !url.contains("keycloak");
            boolean hasSession = page.context().cookies().stream()
                    .anyMatch(c -> "JSESSIONID".equalsIgnoreCase(c.name));
            return onStudio && hasSession;
        }, 20000, 250, "Waiting for SSO login to land back on Studio with a session cookie");
        return new EditorPage();
    }
}
