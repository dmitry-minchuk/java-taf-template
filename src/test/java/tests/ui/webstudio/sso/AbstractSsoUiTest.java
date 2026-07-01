package tests.ui.webstudio.sso;

import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.KeycloakLoginPage;
import helpers.service.KeycloakInfrastructureService;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import tests.BaseTest;

/**
 * PLAYWRIGHT_DOCKER base for external-IdP (Keycloak) UI tests. Owns the ephemeral Keycloak
 * lifecycle and the SSO login helper shared by the OIDC/SAML suites; the browser must share the
 * Docker network with Keycloak so the issuer URL is identical for browser, Studio and the token.
 */
public abstract class AbstractSsoUiTest extends BaseTest {

    protected final KeycloakInfrastructureService keycloak = new KeycloakInfrastructureService();

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        if (!"PLAYWRIGHT_DOCKER".equalsIgnoreCase(System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL"))) {
            throw new SkipException("External-auth SSO test requires -Dexecution.mode=PLAYWRIGHT_DOCKER "
                    + "(browser must share the Docker network with Keycloak).");
        }
        keycloak.start();
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        try {
            super.afterMethod(result);
        } finally {
            keycloak.stop();
        }
    }

    /** Clears the browser session so the next SSO login starts fresh (switches IdP user). */
    protected EditorPage ssoLogin(String username, String password) {
        LocalDriverPool.getBrowserContext().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        return new KeycloakLoginPage().login(username, password);
    }
}
