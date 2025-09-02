package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

public class WaitUtil {

    private static final Logger LOGGER = LogManager.getLogger(WaitUtil.class);

    public static void sleep(int timeoutMillis) {
        try {
            Thread.sleep(timeoutMillis);
            LOGGER.info("Thread.sleep({}) here...", timeoutMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void waitForListNotEmpty(Supplier<List<T>> listSupplier,
                                               long timeoutMs,
                                               long pollingIntervalMs) {
        if (!isListNotEmpty(listSupplier, timeoutMs, pollingIntervalMs)) {
            throw new RuntimeException("List remained empty after " + timeoutMs + "ms timeout");
        }
    }

    public static <T> boolean isListNotEmpty(Supplier<List<T>> listSupplier,
                                             long timeoutMs,
                                             long pollingIntervalMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < endTime) {
            List<T> currentList = listSupplier.get();
            if (currentList != null && !currentList.isEmpty()) {
                return true;
            }
            LOGGER.info("Waiting for collection not to be empty...");
            sleep((int) pollingIntervalMs);
        }
        return false;
    }
}