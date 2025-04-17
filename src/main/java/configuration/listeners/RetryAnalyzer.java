package configuration.listeners;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryAnalyzer implements IRetryAnalyzer {

    private AtomicInteger retries = new AtomicInteger(Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.RETRY_COUNT)));

    @Override
    public boolean retry(ITestResult result) {
        return retries.decrementAndGet() > 0;
    }
}
