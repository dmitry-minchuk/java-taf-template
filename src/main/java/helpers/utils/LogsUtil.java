package helpers.utils;

import configuration.appcontainer.AppContainerData;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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

    // Log analysis here:

    static String pathLogDefault = "/opt/openl/logs/webstudio.log";
    static String pathLogDefaultService = "/opt/openl/logs/rulservice.log";
    static String pathLogDemoStudio = "/demo/logs/webstudio.log";
    static String pathLogDemoService = "/demo/logs/rulservice.log";
    static List<String> exclusions = new ArrayList<>();
    static {
        exclusions.add("Possible broken OpenAPI file");
        //exclusions.add("Name cannot contain forbidden characters");
        exclusions.add("repository.upload.AProjectCreator");
        exclusions.add("snakeyaml.parser.ParserException");
        exclusions.add("MarkedYAMLException: while parsing a block mapping");
        exclusions.add("A module with the same name already exists");
        exclusions.add("Cannot find project artefact 'rules.xlsx'");
        exclusions.add("Failed to export file version");
        //exclusions.add("Username and Password not accepted");
        exclusions.add("io.grpc.ManagedChannel.shutdown()");
    }

    public static void inspectLogFile(AppContainerData appContainerData) {
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - use proper container wait strategies instead
        List<String> logs = Arrays.asList(appContainerData.getAppContainer().getLogs().split("\\r?\\n"));
        LOGGER.info("Analyzed rows in log file: " + logs.size());
        String error = "ERROR [";
        String exception = "Exception:";
        List<String> foundErrors = logs.parallelStream().filter(l -> l.contains(error) || l.contains(exception)).toList();
        List<String> filteredErrors = foundErrors.stream().filter(l -> exclusions.stream().noneMatch(l::contains)).toList();
        StringBuilder errorPrint = new StringBuilder();
        filteredErrors.forEach(e -> {
            errorPrint.append("\n");
            errorPrint.append(e);
        });
        assertThat(filteredErrors.isEmpty()).as("Unexpected errors found in logs: " + errorPrint).isTrue();
    }
}
