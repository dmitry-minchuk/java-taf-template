package configuration.driver;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlaywrightTracing {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightTracing.class);
    private static final String TRACE_FILE_NAME = "trace.zip";

    private PlaywrightTracing() {
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(ProjectConfiguration.getProperty(PropertyNameSpace.ENABLE_PLAYWRIGHT_TRACING));
    }

    public static void start(BrowserContext context) {
        if (!isEnabled()) {
            return;
        }
        try {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(false)
                    .setSnapshots(true)
                    .setSources(true));
            LOGGER.info("Playwright tracing started (DOM snapshots, network, console, sources)");
        } catch (RuntimeException e) {
            LOGGER.warn("Playwright tracing could not be started: {}", e.getMessage());
        }
    }

    public static File stop(BrowserContext context, String testName, boolean keep) {
        if (!isEnabled()) {
            return null;
        }
        try {
            if (!keep) {
                context.tracing().stop();
                LOGGER.debug("Playwright trace discarded for test: {}", testName);
                return null;
            }
            Path traceDir = Files.createTempDirectory("playwright-trace-" + testName.replaceAll("[^A-Za-z0-9._-]", "_"));
            Path traceFile = traceDir.resolve(TRACE_FILE_NAME);
            context.tracing().stop(new Tracing.StopOptions().setPath(traceFile));
            LOGGER.info("Playwright trace saved for test {}: {} bytes", testName, Files.size(traceFile));
            return traceFile.toFile();
        } catch (Exception e) {
            LOGGER.warn("Playwright trace could not be saved for test {}: {}", testName, e.getMessage());
            return null;
        }
    }
}
