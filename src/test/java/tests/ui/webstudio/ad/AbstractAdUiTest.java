package tests.ui.webstudio.ad;

import configuration.driver.DriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.SambaAdInfrastructureService;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import tests.BaseTest;

// TODO: figure out why the Studio container start/stop is not delegated to the standard BaseTest
//  @BeforeMethod/@AfterMethod mechanism (separate config methods, like startMailMock/stopMailMock in
//  TestAdminEmail) and is instead intercepted by beforeMethod/afterMethod overrides with an explicit super.
public abstract class AbstractAdUiTest extends BaseTest {

    protected final SambaAdInfrastructureService samba = new SambaAdInfrastructureService();

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        if (!"PLAYWRIGHT_DOCKER".equalsIgnoreCase(System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL"))) {
            throw new SkipException("AD auth test requires -Dexecution.mode=PLAYWRIGHT_DOCKER "
                    + "(Studio must share the Docker network with the Samba AD DC).");
        }
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

    protected EditorPage adLogin(String username, String password) {
        DriverPool.getBrowserContext().clearCookies();
        DriverPool.getPage().navigate(DriverPool.getAppUrl());
        return new LoginPage().login(new UserData(username, password));
    }
}
