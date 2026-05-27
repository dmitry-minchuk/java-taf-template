package helpers.utils;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.epam.reportportal.service.ReportPortal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReportPortalArtifactUtil {

    private static final Logger LOGGER = LogManager.getLogger(ReportPortalArtifactUtil.class);
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path EXPORT_ROOT = Path.of(System.getProperty("rp.export.dir", "target/rp-export"));
    private static final ThreadLocal<TestContext> CURRENT_TEST = new ThreadLocal<>();
    private static final AtomicBoolean RUN_MANIFEST_WRITTEN = new AtomicBoolean(false);

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
        TestContext context = new TestContext(
                result.getTestContext().getSuite().getName(),
                result.getTestContext().getName(),
                method.getDeclaringClass().getName(),
                method.getName(),
                displayName
        );
        CURRENT_TEST.set(context);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("suite", context.suiteName);
        metadata.put("test", context.testName);
        metadata.put("className", context.className);
        metadata.put("methodName", context.methodName);
        metadata.put("displayName", context.displayName);
        metadata.put("startedAt", Instant.ofEpochMilli(result.getStartMillis()).toString());

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

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", statusName(result.getStatus()));
        payload.put("startedAt", Instant.ofEpochMilli(result.getStartMillis()).toString());
        payload.put("finishedAt", Instant.ofEpochMilli(result.getEndMillis()).toString());
        payload.put("durationMs", Math.max(0, result.getEndMillis() - result.getStartMillis()));

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            payload.put("errorType", throwable.getClass().getName());
            payload.put("errorMessage", throwable.getMessage());
            payload.put("stackTrace", stackTrace(throwable));
        }

        writeJson(context.testDirectory().resolve("result.json"), payload);
        CURRENT_TEST.remove();
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

            Map<String, Object> attachment = new LinkedHashMap<>();
            attachment.put("message", message);
            attachment.put("level", level);
            attachment.put("createdAt", Instant.now().toString());
            attachment.put("sourceFileName", source.getName());
            attachment.put("path", EXPORT_ROOT.relativize(target).toString());
            appendJsonLine(context.testDirectory().resolve("attachments.jsonl"), attachment);

            return target.toFile();
        } catch (IOException e) {
            LOGGER.warn("Failed to record ReportPortal export attachment {}: {}", source, e.getMessage());
            return source;
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

    private record TestContext(String suiteName, String testName, String className, String methodName, String displayName) {
        Path testDirectory() {
            return EXPORT_ROOT
                    .resolve("tests")
                    .resolve(StringUtil.sanitizeFileName(className))
                    .resolve(StringUtil.sanitizeFileName(displayName == null || displayName.isBlank() ? methodName : displayName));
        }
    }
}
