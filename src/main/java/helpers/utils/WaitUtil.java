package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class WaitUtil {

    private static final Logger LOGGER = LogManager.getLogger(WaitUtil.class);

    public static void sleep(long timeoutMillis, String description) {
        try {
            LOGGER.info("Sleeping for {}ms: {}", timeoutMillis, description);
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted: " + description, e);
        }
    }

    public static <T> void waitForListNotEmpty(Supplier<List<T>> listSupplier, long timeoutMs, long pollingIntervalMs, String description) {
        LOGGER.info("Waiting for list not to be empty (timeout: {}ms, polling: {}ms): {}", timeoutMs, pollingIntervalMs, description);
        if (!isListNotEmpty(listSupplier, timeoutMs, pollingIntervalMs, description)) {
            throw new RuntimeException("List remained empty after " + timeoutMs + "ms timeout: " + description);
        }
    }

    public static <T> boolean isListNotEmpty(Supplier<List<T>> listSupplier, long timeoutMs, long pollingIntervalMs, String description) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < endTime) {
            List<T> currentList = listSupplier.get();
            if (currentList != null && !currentList.isEmpty()) {
                LOGGER.info("List became non-empty after {}ms: {}", System.currentTimeMillis() - startTime, description);
                return true;
            }
            sleep((int) pollingIntervalMs, "Polling for list (attempt): " + description);
        }
        LOGGER.warn("List remained empty after {}ms timeout: {}", timeoutMs, description);
        return false;
    }

    public static boolean waitForCondition(Supplier<Boolean> conditionSupplier, long timeoutMs, long pollingIntervalMs, String description) {
        LOGGER.info("Waiting for condition (timeout: {}ms, polling: {}ms): {}", timeoutMs, pollingIntervalMs, description);
        long endTime = System.currentTimeMillis() + timeoutMs;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < endTime) {
            Boolean currentState = conditionSupplier.get();
            if (currentState) {
                LOGGER.info("Condition met after {}ms: {}", System.currentTimeMillis() - startTime, description);
                return true;
            }
            sleep((int) pollingIntervalMs, "Polling for condition (attempt): " + description);
        }
        LOGGER.warn("Condition not met after {}ms timeout: {}", timeoutMs, description);
        return false;
    }

    public static <T> Optional<T> waitForResult(Supplier<Optional<T>> supplier, long timeoutMs, long intervalMs, String description) {
        LOGGER.info("Waiting for result (timeout: {}ms, polling: {}ms): {}", timeoutMs, intervalMs, description);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Optional<T> result = supplier.get();
            if (result.isPresent()) {
                LOGGER.info("Result found after {}ms: {}", System.currentTimeMillis() - startTime, description);
                return result;
            }
            sleep(intervalMs, "Polling for result (attempt): " + description);
        }

        LOGGER.warn("Result not found after {}ms timeout: {}", timeoutMs, description);
        return Optional.empty();
    }

    public static boolean retryAction(CheckedRunnable action, long timeoutMs, long pollingIntervalMs, String description) {
        LOGGER.info("Retrying action with polling (timeout: {}ms, polling: {}ms): {}", timeoutMs, pollingIntervalMs, description);
        long endTime = System.currentTimeMillis() + timeoutMs;
        long startTime = System.currentTimeMillis();
        Exception lastException = null;

        while (System.currentTimeMillis() < endTime) {
            try {
                action.run();
                LOGGER.info("Action succeeded after {}ms: {}", System.currentTimeMillis() - startTime, description);
                return true;
            } catch (Exception e) {
                lastException = e;
                LOGGER.debug("Action attempt failed (will retry): {} - {}", description, e.getMessage());
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > 0) {
                    sleep(Math.min(pollingIntervalMs, remainingTime), "Retrying action: " + description);
                }
            }
        }

        LOGGER.warn("Action did not succeed after {}ms timeout: {}", timeoutMs, description);
        if (lastException != null) {
            LOGGER.debug("Last error: {}", lastException.getMessage());
        }
        return false;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}