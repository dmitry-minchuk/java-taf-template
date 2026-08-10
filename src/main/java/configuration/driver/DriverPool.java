package configuration.driver;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import configuration.appcontainer.AppContainerData;
import configuration.appcontainer.AppContainerPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;

/**
 * Unified entry point to Playwright regardless of execution mode. Dispatches every call to
 * {@link LocalDriverPool} or {@link DockerDriverPool} based on {@link ExecutionMode#current()},
 * so components and tests never care where the browser actually runs.
 */
public final class DriverPool {

    private static final Logger LOGGER = LogManager.getLogger(DriverPool.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));

    private DriverPool() {
    }

    public static void initializePlaywright(Network network) {
        switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> LocalDriverPool.setPlaywright();
            case PLAYWRIGHT_DOCKER -> DockerDriverPool.setPlaywrightDocker(network);
        }
        LOGGER.info("Playwright initialized in {} mode", ExecutionMode.current());
    }

    public static Page getPage() {
        return switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> LocalDriverPool.getPage();
            case PLAYWRIGHT_DOCKER -> DockerDriverPool.getPage();
        };
    }

    public static BrowserContext getBrowserContext() {
        return switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> LocalDriverPool.getBrowserContext();
            case PLAYWRIGHT_DOCKER -> DockerDriverPool.getBrowserContext();
        };
    }

    public static void closePlaywright() {
        switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> LocalDriverPool.closePlaywright();
            case PLAYWRIGHT_DOCKER -> DockerDriverPool.closePlaywrightDocker();
        }
    }

    // Create a new page in the current browser context for multiple tabs/pages
    public static Page createNewPage() {
        Page newPage = getBrowserContext().newPage();
        newPage.setDefaultTimeout(DEFAULT_TIMEOUT_MS);
        LOGGER.debug("Created new page in browser context");
        return newPage;
    }

    // Take screenshot with current page for debugging and test reporting
    public static byte[] takeScreenshot() {
        return getPage().screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
    }

    // Navigate to application container URL with automatic mode detection
    public static void navigateToApp() {
        switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> {
                String url = getAppUrl();
                LOGGER.info("Navigating to application via host URL (LOCAL): {}", url);
                getPage().navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(DEFAULT_TIMEOUT_MS));
            }
            case PLAYWRIGHT_DOCKER -> DockerDriverPool.navigateToApp();
        }
    }

    // Get application URL with automatic mode detection for login services
    public static String getAppUrl() {
        ExecutionMode mode = ExecutionMode.current();
        AppContainerData appData = AppContainerPool.get();
        if (appData == null) {
            throw new IllegalStateException("No application container found while resolving app URL for mode " + mode);
        }

        switch (mode) {
            case PLAYWRIGHT_LOCAL -> {
                // For local mode, use mapped port URL (Playwright runs on host)
                var container = appData.getAppContainer();
                int defaultAppPort = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
                Integer mappedPort = container.getMappedPort(defaultAppPort);
                String deployedAppPath = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);

                String hostUrl = String.format("http://localhost:%d%s", mappedPort, deployedAppPath);
                LOGGER.info("App URL (LOCAL): {}", hostUrl);
                return hostUrl;
            }
            case PLAYWRIGHT_DOCKER -> {
                // For Docker mode, use container network URL (Playwright runs in container)
                String containerNetworkUrl = appData.getAppHostUrl(); // Already contains container network URL
                LOGGER.info("App URL (DOCKER): {}", containerNetworkUrl);
                return containerNetworkUrl;
            }
            default -> throw new UnsupportedOperationException("Unknown execution mode: " + mode);
        }
    }

    public static ExecutionMode getCurrentExecutionMode() {
        return ExecutionMode.current();
    }

    public static String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append(String.format("Execution Mode: %s%n", ExecutionMode.current()));

        switch (ExecutionMode.current()) {
            case PLAYWRIGHT_LOCAL -> info.append(LocalDriverPool.getLocalDebugInfo());
            case PLAYWRIGHT_DOCKER -> info.append(DockerDriverPool.getDockerInfo());
        }

        info.append("\nConfiguration:\n");
        info.append(String.format("Host Resource Path: %s%n",
                ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH)));
        return info.toString();
    }

    // Runs a single cleanup step, logging instead of propagating so the remaining steps still run.
    static void closeQuietly(String what, Runnable closeAction) {
        try {
            closeAction.run();
            LOGGER.debug("Closed {} successfully", what);
        } catch (Exception e) {
            LOGGER.warn("Error closing {}: {}", what, e.getMessage());
        }
    }
}
