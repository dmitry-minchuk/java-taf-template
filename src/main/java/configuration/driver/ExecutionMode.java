package configuration.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright execution mode, resolved once per JVM from the {@code execution.mode} system property.
 * The single source of truth for mode detection — do not re-parse the property elsewhere.
 */
public enum ExecutionMode {
    PLAYWRIGHT_LOCAL,   // Playwright runs on the host machine
    PLAYWRIGHT_DOCKER;  // Playwright runs in a Docker container

    private static final Logger LOGGER = LogManager.getLogger(ExecutionMode.class);
    private static final ExecutionMode CURRENT = resolve();

    public static ExecutionMode current() {
        return CURRENT;
    }

    private static ExecutionMode resolve() {
        String mode = System.getProperty("execution.mode", PLAYWRIGHT_LOCAL.name());
        try {
            ExecutionMode execMode = valueOf(mode.toUpperCase());
            LOGGER.info("Using execution mode: {}", execMode);
            return execMode;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown execution mode '{}', defaulting to PLAYWRIGHT_LOCAL", mode);
            return PLAYWRIGHT_LOCAL;
        }
    }
}
