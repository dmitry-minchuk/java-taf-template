package helpers.utils;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.epam.reportportal.service.ReportPortal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReportPortalArtifactUtil {

    private static final Logger LOGGER = LogManager.getLogger(ReportPortalArtifactUtil.class);
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path EXPORT_ROOT = Path.of(System.getProperty("rp.export.dir", "target/rp-export"));
    private static final ThreadLocal<TestContext> CURRENT_TEST = new ThreadLocal<>();
    private static final AtomicBoolean RUN_MANIFEST_WRITTEN = new AtomicBoolean(false);
    private static final AtomicBoolean STEP_LOG_APPENDER_INSTALLED = new AtomicBoolean(false);
    private static final String STEP_LOG_CONTEXT_KEY = "rpExportTestDir";
    private static final String STEP_LOG_FILE_NAME = "test-steps.log";
    private static final String STEP_LOG_MESSAGE = "Test Steps Log";
    private static final String STEP_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] [%p] %c{1} - %m%n%throwable{full}";
    private static final Map<String, Writer> STEP_LOG_WRITERS = new ConcurrentHashMap<>();

    private ReportPortalArtifactUtil() {
    }

    public static boolean isReportPortalEnabled() {
        return Boolean.parseBoolean(System.getProperty("rp.enable", "true"));
    }

    public static boolean emitLog(String message, String level, File file) {
        if (!isReportPortalEnabled()) {
            LOGGER.debug("ReportPortal is disabled; skipped emitLog for {}", file);
            return false;
        }
        if (file == null || !file.exists()) {
            return ReportPortal.emitLog(message, level, java.util.Date.from(Instant.now()));
        }
        return ReportPortal.emitLog(message, level, java.util.Date.from(Instant.now()), file);
    }

    public static void startTest(ITestResult result, String displayName) {
        writeRunManifest();

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        org.testng.ITestContext testContext = result.getTestContext();
        TestContext context = contextFor(result, method, displayName);
        CURRENT_TEST.set(context);

        writeMetadata(result, method, context);
        startStepLog(context);
    }

    private static void startStepLog(TestContext context) {
        installStepLogAppender();
        ThreadContext.put(STEP_LOG_CONTEXT_KEY, context.testDirectory().toString());
    }

    private static synchronized void installStepLogAppender() {
        if (STEP_LOG_APPENDER_INSTALLED.get()) {
            return;
        }
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(STEP_LOG_PATTERN)
                .withConfiguration(configuration)
                .build();
        TestStepLogAppender appender = new TestStepLogAppender(layout);
        appender.start();
        configuration.addAppender(appender);

        Set<LoggerConfig> targets = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        targets.add(configuration.getRootLogger());
        configuration.getLoggers().values().stream()
                .filter(loggerConfig -> !loggerConfig.isAdditive())
                .forEach(targets::add);
        targets.forEach(loggerConfig -> loggerConfig.addAppender(appender, null, null));
        loggerContext.updateLoggers();
        STEP_LOG_APPENDER_INSTALLED.set(true);
    }

    private static Writer stepLogWriter(String testDirectory) throws IOException {
        Writer existing = STEP_LOG_WRITERS.get(testDirectory);
        if (existing != null) {
            return existing;
        }
        Path logFile = Path.of(testDirectory).resolve("attachments").resolve(STEP_LOG_FILE_NAME);
        Files.createDirectories(logFile.getParent());
        Writer opened = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Writer raced = STEP_LOG_WRITERS.putIfAbsent(testDirectory, opened);
        if (raced != null) {
            opened.close();
            return raced;
        }
        return opened;
    }

    private static void finishStepLog(TestContext context) {
        ThreadContext.remove(STEP_LOG_CONTEXT_KEY);
        String testDirectory = context.testDirectory().toString();
        Writer writer = STEP_LOG_WRITERS.remove(testDirectory);
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close test steps log in {}: {}", testDirectory, e.getMessage());
            }
        }
        Path logFile = context.testDirectory().resolve("attachments").resolve(STEP_LOG_FILE_NAME);
        if (Files.exists(logFile) && !isIndexed(context, logFile)) {
            registerAttachment(context, STEP_LOG_MESSAGE, "INFO", logFile, STEP_LOG_FILE_NAME);
        }
    }

    private static boolean isIndexed(TestContext context, Path attachment) {
        Path index = context.testDirectory().resolve("attachments.jsonl");
        if (!Files.exists(index)) {
            return false;
        }
        try {
            return Files.readString(index).contains(EXPORT_ROOT.relativize(attachment).toString());
        } catch (IOException e) {
            return false;
        }
    }

    private static final class TestStepLogAppender extends AbstractAppender {

        private final PatternLayout patternLayout;

        private TestStepLogAppender(PatternLayout layout) {
            super("RpExportTestStepLog", null, layout, true, Property.EMPTY_ARRAY);
            this.patternLayout = layout;
        }

        @Override
        public void append(LogEvent event) {
            String testDirectory = event.getContextData().getValue(STEP_LOG_CONTEXT_KEY);
            if (testDirectory == null) {
                return;
            }
            String line = patternLayout.toSerializable(event);
            try {
                Writer writer = stepLogWriter(testDirectory);
                synchronized (writer) {
                    writer.write(line);
                    writer.flush();
                }
            } catch (IOException e) {
                STEP_LOG_WRITERS.remove(testDirectory);
                getHandler().error("Failed to write test steps log in " + testDirectory, e);
            }
        }
    }

    public static void recordTestResultIfMissing(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        TestContext context = contextFor(result, method, displayName(result));
        if (Files.exists(context.testDirectory().resolve("result.json"))) {
            return;
        }

        writeRunManifest();
        writeMetadata(result, method, context);
        writeResult(result, context);
    }

    private static void writeMetadata(ITestResult result, Method method, TestContext context) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("suite", context.suiteName);
        metadata.put("test", context.testName);
        metadata.put("className", context.className);
        metadata.put("methodName", context.methodName);
        metadata.put("displayName", context.displayName);
        metadata.put("startedAt", instant(result.getStartMillis()).toString());

        TestCaseId testCaseId = method.getAnnotation(TestCaseId.class);
        if (testCaseId != null) {
            metadata.put("testCaseId", testCaseId.value());
        }
        Description description = method.getAnnotation(Description.class);
        if (description != null) {
            metadata.put("description", description.value());
        }

        writeJson(context.testDirectory().resolve("metadata.json"), metadata);
    }

    public static void finishTest(ITestResult result) {
        TestContext context = CURRENT_TEST.get();
        if (context == null) {
            LOGGER.warn("Cannot write ReportPortal export result: current test context is not set");
            return;
        }

        writeResult(result, context);
        finishStepLog(context);
        CURRENT_TEST.remove();
    }

    private static void writeResult(ITestResult result, TestContext context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", statusName(result.getStatus()));
        payload.put("startedAt", instant(result.getStartMillis()).toString());
        payload.put("finishedAt", instant(result.getEndMillis()).toString());
        payload.put("durationMs", Math.max(0, result.getEndMillis() - result.getStartMillis()));

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            payload.put("errorType", throwable.getClass().getName());
            payload.put("errorMessage", throwable.getMessage());
            payload.put("stackTrace", stackTrace(throwable));
        }

        writeJson(context.testDirectory().resolve("result.json"), payload);
    }

    public static File recordAttachment(String message, String level, File source) {
        if (source == null || !source.exists()) {
            LOGGER.warn("Cannot record ReportPortal export attachment; file does not exist: {}", source);
            return source;
        }

        TestContext context = CURRENT_TEST.get();
        if (context == null) {
            LOGGER.debug("Current test context is not set; attachment will not be copied to rp-export: {}", source);
            return source;
        }

        try {
            Path attachmentsDir = context.testDirectory().resolve("attachments");
            Files.createDirectories(attachmentsDir);

            String fileName = System.currentTimeMillis() + "-" + StringUtil.sanitizeFileName(source.getName());
            Path target = attachmentsDir.resolve(fileName);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            registerAttachment(context, message, level, target, source.getName());
            return target.toFile();
        } catch (IOException e) {
            LOGGER.warn("Failed to record ReportPortal export attachment {}: {}", source, e.getMessage());
            return source;
        }
    }

    private static void registerAttachment(TestContext context, String message, String level, Path target, String sourceFileName) {
        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("message", message);
        attachment.put("level", level);
        attachment.put("createdAt", Instant.now().toString());
        attachment.put("sourceFileName", sourceFileName);
        attachment.put("path", EXPORT_ROOT.relativize(target).toString());
        try {
            appendJsonLine(context.testDirectory().resolve("attachments.jsonl"), attachment);
        } catch (IOException e) {
            LOGGER.warn("Failed to index ReportPortal export attachment {}: {}", target, e.getMessage());
        }
    }

    public static void writeRunManifest() {
        if (!RUN_MANIFEST_WRITTEN.compareAndSet(false, true)) {
            return;
        }

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("schemaVersion", 1);
        manifest.put("createdAt", Instant.now().toString());
        manifest.put("suite", System.getProperty("suite", ""));
        manifest.put("rpLaunch", System.getProperty("rp.launch", System.getProperty("suite", "")));
        manifest.put("rpProject", System.getProperty("rp.project", ""));
        manifest.put("rpAttributes", System.getProperty("rp.attributes", ""));
        manifest.put("gitBranch", firstNonBlank(System.getenv("GITHUB_REF_NAME"), System.getProperty("TESTS_BRANCH"), System.getenv("BRANCH_NAME")));
        manifest.put("gitSha", firstNonBlank(System.getenv("GITHUB_SHA"), System.getenv("GIT_COMMIT")));
        manifest.put("githubRunId", System.getenv("GITHUB_RUN_ID"));
        manifest.put("githubRunNumber", System.getenv("GITHUB_RUN_NUMBER"));
        manifest.put("githubWorkflow", System.getenv("GITHUB_WORKFLOW"));
        manifest.put("githubRepository", System.getenv("GITHUB_REPOSITORY"));
        manifest.put("reportPortalEnabled", isReportPortalEnabled());

        writeJson(EXPORT_ROOT.resolve("manifest.json"), manifest);
    }

    private static String statusName(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "FAILED";
            default -> "UNKNOWN";
        };
    }

    private static String stackTrace(Throwable throwable) {
        java.io.StringWriter writer = new java.io.StringWriter();
        throwable.printStackTrace(new java.io.PrintWriter(writer));
        return writer.toString();
    }

    private static synchronized void appendJsonLine(Path path, Map<String, Object> payload) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, JSON.writeValueAsString(payload).replace(System.lineSeparator(), "") + System.lineSeparator(),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    private static void writeJson(Path path, Map<String, Object> payload) {
        try {
            Files.createDirectories(path.getParent());
            JSON.writeValue(path.toFile(), payload);
        } catch (IOException e) {
            LOGGER.warn("Failed to write ReportPortal export file {}: {}", path, e.getMessage());
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static TestContext contextFor(ITestResult result, Method method, String displayName) {
        org.testng.ITestContext testContext = result.getTestContext();
        String suiteName = testContext != null ? testContext.getSuite().getName() : "ad-hoc";
        String testName = testContext != null ? testContext.getName() : method.getDeclaringClass().getSimpleName();
        return new TestContext(
                suiteName,
                testName,
                method.getDeclaringClass().getName(),
                method.getName(),
                displayName
        );
    }

    private static String displayName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length == 0) {
            return methodName;
        }

        StringBuilder name = new StringBuilder(methodName).append("[");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                name.append(", ");
            }
            name.append(parameters[i] == null ? "null" : StringUtil.sanitizeFileName(parameters[i].toString()));
        }
        return name.append("]").toString();
    }

    private static Instant instant(long epochMillis) {
        return epochMillis > 0 ? Instant.ofEpochMilli(epochMillis) : Instant.now();
    }

    private record TestContext(String suiteName, String testName, String className, String methodName, String displayName) {
        Path testDirectory() {
            return EXPORT_ROOT
                    .resolve("tests")
                    .resolve(StringUtil.sanitizeFileName(className))
                    .resolve(StringUtil.sanitizeFileName(displayName == null || displayName.isBlank() ? methodName : displayName));
        }
    }
}
