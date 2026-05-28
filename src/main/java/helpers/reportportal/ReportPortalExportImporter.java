package helpers.reportportal;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.utils.http.HttpRequestUtils;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportPortalExportImporter {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {
    };

    public static void main(String[] args) throws IOException {
        Options options = Options.parse(args);
        Map<String, Object> manifest = readMap(options.exportDir.resolve("manifest.json"));
        List<TestExport> tests = readTests(options.exportDir);
        if (tests.isEmpty()) {
            throw new IllegalStateException("No exported tests found in " + options.exportDir);
        }

        if (options.dryRun) {
            System.out.printf("Dry run: %d test item(s) found in %s%n", tests.size(), options.exportDir);
            return;
        }

        ReportPortalClient client = createClient(options);
        String launchUuid = startLaunch(client, options, manifest, tests);
        boolean hasFailures = false;

        for (TestExport test : tests) {
            String itemUuid = startTestItem(client, launchUuid, test);
            sendLogs(client, options.exportDir, launchUuid, itemUuid, test);
            finishTestItem(client, itemUuid, launchUuid, test);
            hasFailures |= !"PASSED".equals(test.status());
        }

        finishLaunch(client, launchUuid, hasFailures ? "FAILED" : "PASSED");
        System.out.printf("Imported %d test item(s) into ReportPortal launch %s%n", tests.size(), launchUuid);
    }

    private static ReportPortalClient createClient(Options options) {
        ListenerParameters parameters = new ListenerParameters();
        parameters.setBaseUrl(options.endpoint);
        parameters.setApiKey(options.apiKey);
        parameters.setProjectName(options.project);
        return ReportPortal.builder().withParameters(parameters).build().getClient();
    }

    private static String startLaunch(ReportPortalClient client, Options options, Map<String, Object> manifest, List<TestExport> tests) {
        StartLaunchRQ request = new StartLaunchRQ();
        request.setName(options.launchName != null ? options.launchName : stringValue(manifest, "rpLaunch", "GitHub Actions import"));
        request.setDescription("Imported from GitHub Actions artifact"
                + line("workflow", manifest.get("githubWorkflow"))
                + line("run", manifest.get("githubRunId"))
                + line("repository", manifest.get("githubRepository"))
                + line("sha", manifest.get("gitSha")));
        request.setAttributes(attributes(stringValue(manifest, "rpAttributes", ""), manifest));
        request.setStartTime(tests.stream()
                .map(TestExport::startedAt)
                .min(Date::compareTo)
                .orElse(new Date()));

        StartLaunchRS response = client.startLaunch(request).blockingGet();
        return response.getId();
    }

    private static String startTestItem(ReportPortalClient client, String launchUuid, TestExport test) {
        StartTestItemRQ request = new StartTestItemRQ();
        request.setLaunchUuid(launchUuid);
        request.setName(test.displayName());
        request.setCodeRef(test.className() + "." + test.methodName());
        request.setDescription(test.description());
        request.setType("TEST");
        request.setStartTime(test.startedAt());
        if (!test.testCaseId().isBlank()) {
            request.setTestCaseId(test.testCaseId());
        }

        ItemCreatedRS response = client.startTestItem(request).blockingGet();
        return response.getId();
    }

    private static void sendLogs(ReportPortalClient client, Path exportDir, String launchUuid, String itemUuid, TestExport test) throws IOException {
        if (test.errorMessage() != null && !test.errorMessage().isBlank()) {
            SaveLogRQ error = baseLog(launchUuid, itemUuid, "ERROR", test.finishedAt(),
                    test.errorMessage() + System.lineSeparator() + nullToBlank(test.stackTrace()));
            client.log(error).blockingGet();
        }

        for (Map<String, Object> attachment : test.attachments()) {
            Path file = exportDir.resolve(stringValue(attachment, "path", ""));
            if (!Files.exists(file)) {
                continue;
            }

            SaveLogRQ log = baseLog(
                    launchUuid,
                    itemUuid,
                    stringValue(attachment, "level", "INFO"),
                    parseDate(stringValue(attachment, "createdAt", null), test.finishedAt()),
                    stringValue(attachment, "message", file.getFileName().toString())
            );
            SaveLogRQ.File rpFile = new SaveLogRQ.File();
            rpFile.setName(file.getFileName().toString());
            rpFile.setContent(Files.readAllBytes(file));
            rpFile.setContentType(contentType(file));
            log.setFile(rpFile);
            client.log(HttpRequestUtils.buildLogMultiPartRequest(List.of(log))).blockingGet();
        }
    }

    private static SaveLogRQ baseLog(String launchUuid, String itemUuid, String level, Date time, String message) {
        SaveLogRQ log = new SaveLogRQ();
        log.setLaunchUuid(launchUuid);
        log.setItemUuid(itemUuid);
        log.setLevel(level);
        log.setLogTime(time);
        log.setMessage(message);
        return log;
    }

    private static void finishTestItem(ReportPortalClient client, String itemUuid, String launchUuid, TestExport test) {
        FinishTestItemRQ request = new FinishTestItemRQ();
        request.setLaunchUuid(launchUuid);
        request.setStatus(test.status());
        request.setEndTime(test.finishedAt());
        client.finishTestItem(itemUuid, request).blockingGet();
    }

    private static void finishLaunch(ReportPortalClient client, String launchUuid, String status) {
        FinishExecutionRQ request = new FinishExecutionRQ();
        request.setEndTime(new Date());
        request.setStatus(status);
        client.finishLaunch(launchUuid, request).blockingGet();
    }

    private static List<TestExport> readTests(Path exportDir) throws IOException {
        Path testsRoot = exportDir.resolve("tests");
        if (!Files.exists(testsRoot)) {
            return List.of();
        }

        List<TestExport> tests = new ArrayList<>();
        try (var paths = Files.walk(testsRoot)) {
            for (Path metadataPath : paths.filter(path -> path.getFileName().toString().equals("metadata.json")).toList()) {
                Path testDir = metadataPath.getParent();
                Path resultPath = testDir.resolve("result.json");
                if (!Files.exists(resultPath)) {
                    continue;
                }
                tests.add(TestExport.from(testDir, readMap(metadataPath), readMap(resultPath)));
            }
        }
        tests.sort(Comparator.comparing(TestExport::startedAt));
        return tests;
    }

    private static Map<String, Object> readMap(Path path) throws IOException {
        return JSON.readValue(path.toFile(), MAP);
    }

    private static List<Map<String, Object>> readJsonLines(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String line : Files.readAllLines(path)) {
            if (!line.isBlank()) {
                result.add(JSON.readValue(line, MAP));
            }
        }
        return result;
    }

    private static Set<ItemAttributesRQ> attributes(String rawAttributes, Map<String, Object> manifest) {
        Set<ItemAttributesRQ> attributes = new LinkedHashSet<>();
        for (String token : rawAttributes.split(";")) {
            if (token.isBlank()) {
                continue;
            }
            String[] parts = token.split(":", 2);
            if (parts.length == 2) {
                attributes.add(new ItemAttributesRQ(parts[0].trim(), parts[1].trim()));
            } else {
                attributes.add(new ItemAttributesRQ(token.trim()));
            }
        }
        addAttribute(attributes, "github_run_id", manifest.get("githubRunId"));
        addAttribute(attributes, "github_run_number", manifest.get("githubRunNumber"));
        addAttribute(attributes, "git_sha", manifest.get("gitSha"));
        return attributes;
    }

    private static void addAttribute(Set<ItemAttributesRQ> attributes, String key, Object value) {
        if (value != null && !value.toString().isBlank()) {
            attributes.add(new ItemAttributesRQ(key, value.toString()));
        }
    }

    private static String contentType(Path file) throws IOException {
        String detected = Files.probeContentType(file);
        if (detected != null) {
            return detected;
        }
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".webm")) {
            return "video/webm";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".html")) {
            return "text/html";
        }
        return "application/octet-stream";
    }

    private static Date parseDate(String value, Date fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Date.from(Instant.parse(value));
    }

    private static String line(String label, Object value) {
        return value == null || value.toString().isBlank() ? "" : System.lineSeparator() + "- " + label + ": `" + value + "`";
    }

    private static String stringValue(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : value.toString();
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private record TestExport(
            String className,
            String methodName,
            String displayName,
            String description,
            String testCaseId,
            String status,
            Date startedAt,
            Date finishedAt,
            String errorMessage,
            String stackTrace,
            List<Map<String, Object>> attachments
    ) {
        static TestExport from(Path testDir, Map<String, Object> metadata, Map<String, Object> result) throws IOException {
            return new TestExport(
                    stringValue(metadata, "className", ""),
                    stringValue(metadata, "methodName", ""),
                    stringValue(metadata, "displayName", stringValue(metadata, "methodName", "")),
                    stringValue(metadata, "description", ""),
                    stringValue(metadata, "testCaseId", ""),
                    stringValue(result, "status", "UNKNOWN"),
                    parseDate(stringValue(result, "startedAt", stringValue(metadata, "startedAt", null)), new Date()),
                    parseDate(stringValue(result, "finishedAt", null), new Date()),
                    stringValue(result, "errorMessage", ""),
                    stringValue(result, "stackTrace", ""),
                    readJsonLines(testDir.resolve("attachments.jsonl"))
            );
        }
    }

    private record Options(Path exportDir, String endpoint, String apiKey, String project, String launchName, boolean dryRun) {
        static Options parse(String[] args) {
            Map<String, String> values = new java.util.HashMap<>();
            for (int i = 0; i < args.length; i++) {
                if ("--dry-run".equals(args[i])) {
                    values.put("dry-run", "true");
                    continue;
                }
                if (args[i].startsWith("--")) {
                    if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                        throw new IllegalArgumentException("Missing value for " + args[i]);
                    }
                    values.put(args[i].substring(2), args[++i]);
                }
            }
            boolean dryRun = Boolean.parseBoolean(values.getOrDefault("dry-run", "false"));
            return new Options(
                    Path.of(required(values, "export-dir")),
                    dryRun ? values.get("rp-endpoint") : required(values, "rp-endpoint"),
                    dryRun ? values.get("rp-api-key") : required(values, "rp-api-key"),
                    dryRun ? values.get("rp-project") : required(values, "rp-project"),
                    values.get("rp-launch"),
                    dryRun
            );
        }

        private static String required(Map<String, String> values, String key) {
            String value = values.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required option --" + key);
            }
            return value;
        }
    }
}
