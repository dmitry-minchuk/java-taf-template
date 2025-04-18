package tests;

import com.epam.reportportal.service.ReportPortal;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.network.NetworkPool;
import helpers.utils.ScreenShotUtil;
import helpers.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        Network network = Network.newNetwork();
        DriverPool.setDriver(network);
        NetworkPool.setNetwork(network);
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            String copyFileFromPath = configAnnotation.copyFileFromPath().isEmpty() ? null : configAnnotation.copyFileFromPath();
            String copyFileToContainerPath = configAnnotation.copyFileToContainerPath().isEmpty() ? null : configAnnotation.copyFileToContainerPath();
            AppContainerPool.setAppContainer(appContainerName, network, containerConfig, copyFileFromPath, copyFileToContainerPath);
        } else {
            AppContainerPool.setAppContainer(appContainerName, network, AppContainerStartParameters.EMPTY.getParameterMap(), null, null);
        }
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            File screenShot = ScreenShotUtil.takeAndSaveScreenshot(DriverPool.getDriver(), ScreenShotUtil.generateScreenshotName(result.getName()));
            if (screenShot != null)
                ReportPortal.emitLog("Test Failure Screenshot", "INFO", new Date(), screenShot);
        }
        DriverPool.closeDriver();
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();
    }
}
