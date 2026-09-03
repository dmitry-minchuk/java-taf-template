package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProblemsPanelComponent extends BaseComponent {

    private static final String COMPILATION_COMPLETE = "Loaded 100%";
    private static final Pattern PROGRESS_BAR_TEXT = Pattern.compile("Loaded \\d+% \\((\\d+)/(\\d+)\\)");
    private static final Set<String> FINISHED_STATES = Set.of("ok", "warnings", "errors");
    private static final int QUIET_POLLS_BEFORE_COMPILED = 4;
    private static final long COMPILATION_TIMEOUT_MS = 30000;
    private static final long COMPILATION_POLL_MS = 250;
    private static final String SERVER_STATUS_SCRIPT = """
            async () => {
                const api = globalThis.openl && globalThis.openl.projectStatus;
                const id = globalThis.projectId;
                if (!api || !id) {
                    return null;
                }
                const status = await api.fetch(id);
                const compilation = status.compilation || {};
                const messages = compilation.messages || {};
                const modules = compilation.modules || {};
                return {
                    compileState: status.compileState || '',
                    errors: messages.errors || 0,
                    warnings: messages.warnings || 0,
                    compiled: modules.compiled || 0,
                    total: modules.total || 0
                };
            }
            """;

    private WebElement showProblemsLink;
    private WebElement hideProblemPanelLink;
    private WebElement errorsCounter;
    private WebElement warningsCounter;
    private WebElement compilationProgressBar;
    private WebElement compilationProgressBarNotSavedProject;
    private List<WebElement> errorElements;
    private List<WebElement> warningElements;

    public ProblemsPanelComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public ProblemsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        showProblemsLink = createScopedElement(".//div[@class='ui-layout-toggler ui-layout-toggler-south ui-layout-toggler-closed ui-layout-toggler-south-closed' and @title='Open']", "showProblemsLink");
        hideProblemPanelLink = createScopedElement(".//div[@id='bottom']//span[@id='south-closer']", "hideProblemPanelLink");
        errorsCounter = createScopedElement("#errors-count", "errorsCounter");
        warningsCounter = createScopedElement("#warnings-count", "warningsCounter");
        compilationProgressBar = createScopedElement("xpath=.//div[@class='panel']//div[@id='progress-info-panel']", "compilationProgressBar");
        compilationProgressBarNotSavedProject = new WebElement(page, "xpath=//div[contains(@class,'ui-layout-resizer')]//div[@class='messagePanel']", "compilationProgressBarNotSavedProject");
        errorElements = createScopedElementList("xpath=.//div[@id='errors-panel']//a", "errorElements");
        warningElements = createScopedElementList("xpath=.//div[@id='warnings-panel']//a", "warningElements");
    }

    public record ServerCompileStatus(String compileState, int errors, int warnings, int compiled, int total) {
        public boolean isCompiling() {
            return "compiling".equals(compileState);
        }

        public boolean isFinished() {
            return FINISHED_STATES.contains(compileState) && compiled == total;
        }
    }

    public void showProblemsPanel() {
        if (showProblemsLink.isVisible()) {
            showProblemsLink.click();
        }
    }

    public void hideProblemsPanel() {
        if (hideProblemPanelLink.isVisible()) {
            hideProblemPanelLink.click();
        }
    }

    public int getErrorsCount() {
        showProblemsPanel();
        waitForCompilationToComplete();
        return parseCounter(errorsCounter.sleep(200).getText());
    }

    public int getWarningsCount() {
        showProblemsPanel();
        waitForCompilationToComplete();
        return parseCounter(warningsCounter.getText());
    }

    private static int parseCounter(String text) {
        return text != null && !text.isBlank() ? Integer.parseInt(text.trim()) : 0;
    }

    public boolean isCompilationInProgress() {
        return !getCompilationProgressBarText().contains(COMPILATION_COMPLETE);
    }

    private boolean areAllModulesCompiled() {
        Matcher matcher = PROGRESS_BAR_TEXT.matcher(getCompilationProgressBarText());
        return matcher.find() && matcher.group(1).equals(matcher.group(2));
    }

    public ServerCompileStatus fetchServerCompileStatusViaPage() {
        try {
            Object result = page.evaluate(SERVER_STATUS_SCRIPT);
            if (!(result instanceof Map<?, ?> map)) {
                return null;
            }
            return new ServerCompileStatus(String.valueOf(map.get("compileState")),
                    ((Number) map.get("errors")).intValue(), ((Number) map.get("warnings")).intValue(),
                    ((Number) map.get("compiled")).intValue(), ((Number) map.get("total")).intValue());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not read the project status from the server: {}", e.getMessage());
            return null;
        }
    }

    public boolean hasErrors() {
        return getErrorsCount() > 0;
    }

    public boolean hasWarnings() {
        return getWarningsCount() > 0;
    }

    public boolean isProblemsPanelVisible() {
        return hideProblemPanelLink.isVisible();
    }

    public String getProblemsInfo() {
        return String.format("Errors: %d, Warnings: %d", getErrorsCount(), getWarningsCount());
    }

    public void checkNoProblems() {
        showProblemsPanel();
        boolean compiled = waitForCompilationToComplete(COMPILATION_TIMEOUT_MS, COMPILATION_POLL_MS);
        if (!compiled) {
            throw new AssertionError("Compilation did not finish within " + COMPILATION_TIMEOUT_MS + " ms, progress bar: '"
                    + getCompilationProgressBarText() + "', server status: " + fetchServerCompileStatusViaPage());
        }
        boolean noProblems = WaitUtil.waitForCondition(
                () -> {
                    try {
                        return parseCounter(errorsCounter.getText()) == 0 && parseCounter(warningsCounter.getText()) == 0;
                    } catch (RuntimeException e) {
                        return false;
                    }
                },
                DEFAULT_TIMEOUT_MS, 500, "Waiting for the problems panel to report no errors and no warnings");
        if (!noProblems) {
            throw new AssertionError("Expected no problems but found: " + getProblemsInfo());
        }
        ServerCompileStatus server = fetchServerCompileStatusViaPage();
        if (server != null && !server.isCompiling() && (server.errors() != 0 || server.warnings() != 0)) {
            throw new AssertionError("The problems panel shows no problems but the server reports " + server);
        }
    }

    public List<String> getAllErrors() {
        showProblemsPanel();
        waitForCompilationToComplete();
        return errorElements.stream()
                .map(WebElement::getText)
                .toList();
    }

    public List<String> getAllWarnings() {
        showProblemsPanel();
        waitForCompilationToComplete();
        return warningElements.stream()
                .map(WebElement::getText)
                .toList();
    }

    public void waitForCompilationToComplete() {
        waitForCompilationToComplete(COMPILATION_TIMEOUT_MS, COMPILATION_POLL_MS);
    }

    public boolean waitForCompilationToComplete(long timeoutMillis, long pollIntervalMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        int quietPolls = 0;
        int allCompiledPolls = 0;
        while (System.currentTimeMillis() < deadline) {
            if (!isCompilationInProgress()) {
                allCompiledPolls = 0;
                if (++quietPolls >= QUIET_POLLS_BEFORE_COMPILED) {
                    LOGGER.info("Compilation completed");
                    return true;
                }
            } else {
                quietPolls = 0;
                if (areAllModulesCompiled()) {
                    if (++allCompiledPolls >= QUIET_POLLS_BEFORE_COMPILED && isFinishedOnServer()) {
                        return true;
                    }
                } else {
                    allCompiledPolls = 0;
                }
            }
            WaitUtil.sleep((int) pollIntervalMillis, "Waiting for project compilation to complete (polling)");
        }
        LOGGER.warn("Compilation timeout reached, progress bar: '{}'", getCompilationProgressBarText());
        return false;
    }

    private boolean isFinishedOnServer() {
        ServerCompileStatus server = fetchServerCompileStatusViaPage();
        if (server == null || !server.isFinished()) {
            return false;
        }
        LOGGER.warn("Progress bar is stuck at '{}' although the server reports {}; the terminal status push was not delivered to the page",
                getCompilationProgressBarText(), server);
        return true;
    }

    public void selectProblemByText(String text) {
        showProblemsPanel();
        waitForCompilationToComplete();
        List<WebElement> allProblems = new ArrayList<>();
        allProblems.addAll(errorElements);
        allProblems.addAll(warningElements);
        allProblems.stream()
                .filter(element -> element.getText().contains(text))
                .findFirst()
                .ifPresent(WebElement::click);
    }

    public void selectProblemByIndex(int index) {
        showProblemsPanel();
        waitForCompilationToComplete();
        if (index > 0 && index <= errorElements.size()) {
            errorElements.get(index - 1).click();
        }
    }

    public boolean isErrorPresent(String errorMessage) {
        showProblemsPanel();
        waitForCompilationToComplete();
        return errorElements.stream()
                .map(WebElement::getText)
                .anyMatch(error -> error.contains(errorMessage));
    }

    public boolean isWarningPresent(String warningMessage) {
        showProblemsPanel();
        waitForCompilationToComplete();
        return warningElements.stream()
                .map(WebElement::getText)
                .anyMatch(warning -> warning.contains(warningMessage));
    }

    public boolean isCompilationProgressBarVisible() {
        return compilationProgressBar.isVisible(1000);
    }

    public boolean isCompilationProgressBarNotSavedProjectVisible() {
        return compilationProgressBarNotSavedProject.isVisible(1000);
    }

    public String getCompilationProgressBarText() {
        try {
            return compilationProgressBar.getText();
        } catch (Exception e) {
            LOGGER.warn("Compilation progress bar is not present, treating the compilation as finished");
            return COMPILATION_COMPLETE;
        }
    }

    public String getCompilationProgressBarNotSavedProjectText() {
        try {
            return compilationProgressBarNotSavedProject.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void waitForCompilationProgressBarToContain(String text, long timeoutMs) {
        WaitUtil.waitForCondition(
                () -> {
                    try {
                        String barText = compilationProgressBarNotSavedProject.getText();
                        return barText != null && barText.contains(text);
                    } catch (Exception e) {
                        return false;
                    }
                },
                timeoutMs, 1000,
                "Waiting for compilation progress bar to contain: " + text
        );
    }
}
