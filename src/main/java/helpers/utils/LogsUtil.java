package helpers.utils;

import configuration.appcontainer.AppContainerData;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LogsUtil {

    private static final Logger LOGGER = LogManager.getLogger(LogsUtil.class);
    private final static String HOST_APP_LOGS_RELATIVE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_APP_LOGS_PATH);

    public static File saveAppLogs(AppContainerData appContainerData) {
        String logs = appContainerData.getAppContainer().getLogs();
        byte[] logsBytes = logs.getBytes();
        String logFileName = StringUtil.generateUniqueName("app-log") + ".txt";
        File hostDir = new File(HOST_APP_LOGS_RELATIVE_PATH);
        File hostFile = new File(hostDir, logFileName);

        if (!hostDir.exists()) {
            hostDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(hostFile)) {
            fos.write(logsBytes);
            LOGGER.info("Logs saved to host: {}", hostFile.getAbsolutePath());
            return hostFile;
        } catch (IOException e) {
            LOGGER.debug("Error while saving logs to host: {}", e.getMessage());
            LOGGER.debug(PrintUtil.prettyPrintObjectCollection.apply(Arrays.stream(e.getStackTrace()).toList()));
            return null;
        }
    }
}
