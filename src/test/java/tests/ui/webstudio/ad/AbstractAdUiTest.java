package tests.ui.webstudio.ad;

import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.SambaAdInfrastructureService;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import tests.BaseTest;

/**
 * PLAYWRIGHT_DOCKER base for Active Directory (LDAP) auth UI tests. Owns the ephemeral Samba AD DC
 * lifecycle and the form-login helper. WebStudio runs in {@code user.mode=ad} against the DC and
 * authenticates via its own login form (not an external IdP), so this uses {@link LoginPage}.
 * Requires DOCKER mode so the Studio container shares the network with the DC ({@code ldap://samba:389}).
 */
public abstract class AbstractAdUiTest extends BaseTest {

    protected final SambaAdInfrastructureService samba = new SambaAdInfrastructureService();

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        if (!"PLAYWRIGHT_DOCKER".equalsIgnoreCase(System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL"))) {
            throw new SkipException("AD auth test requires -Dexecution.mode=PLAYWRIGHT_DOCKER "
                    + "(Studio must share the Docker network with the Samba AD DC).");
        }
        // Start the DC first so it registers the shared network the Studio container then joins (via super).
        samba.start();
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        try {
            super.afterMethod(result);
        } finally {
            samba.stop();
        }
    }

    /** Clears the session and logs in through Studio's form as the given AD user (switches user). */
    protected EditorPage adLogin(String username, String password) {
        LocalDriverPool.getBrowserContext().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        return new LoginPage().login(new UserData(username, password));
    }
}
