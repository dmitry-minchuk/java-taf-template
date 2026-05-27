package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.BasePage;

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

    public EditorPage login(String username, String password) {
        waitForLoginForm();
        usernameField.fillSequentially(username);
        passwordField.fillSequentially(password);
        loginButton.click();
        LocalDriverPool.getPage().waitForLoadState();
        return new EditorPage();
    }
}
