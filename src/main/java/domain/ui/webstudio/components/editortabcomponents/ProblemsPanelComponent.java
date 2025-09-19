package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class ProblemsPanelComponent extends BaseComponent {

    private WebElement showProblemsLink;
    private WebElement hideProblemPanelLink;
    private WebElement errorsCounter;
    private WebElement warningsCounter;
    private WebElement compilationProgressBar;
    private List<WebElement> errorElements;
    private List<WebElement> warningElements;

    public ProblemsPanelComponent() {
        super(LocalDriverPool.getPage());
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
        errorElements = createScopedElementList("xpath=.//div[@id='errors-panel']//a", "errorElements");
        warningElements = createScopedElementList("xpath=.//div[@id='warnings-panel']//a", "warningElements");
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
        String errorText = errorsCounter.getText();
        return errorText != null && !errorText.isEmpty() ? Integer.parseInt(errorText.trim()) : 0;
    }

    public int getWarningsCount() {
        showProblemsPanel();
        waitForCompilationToComplete();
        String warningText = warningsCounter.getText();
        return warningText != null && !warningText.isEmpty() ? Integer.parseInt(warningText.trim()) : 0;
    }

    public boolean isCompilationInProgress() {
        String compilationComplete = "Loaded 100%";
        String compilationStatus;
        try {
            compilationStatus = compilationProgressBar.getText();
        } catch (Exception e) {
            compilationStatus = compilationComplete;
        }
        return !compilationStatus.contains(compilationComplete);
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
        if (hasErrors() || hasWarnings()) {
            throw new AssertionError("Expected no problems but found: " + getProblemsInfo());
        }
    }

    public List<String> getAllErrors() {
        showProblemsPanel();
        waitForCompilationToComplete();
        List<String> errors = errorElements.stream()
                .map(WebElement::getText)
                .toList();
        return errors;
    }

    private void waitForCompilationToComplete() {
        waitForCompilationToComplete(30000, 250);
    }
    
    private void waitForCompilationToComplete(long timeoutMillis, long pollIntervalMillis) {
        long maxAttempts = timeoutMillis / pollIntervalMillis;
        for (int attempt = 1; attempt <= maxAttempts && isCompilationInProgress(); attempt++) {
            LOGGER.info("Compilation in progress, waiting... ({}/{})", attempt, maxAttempts);
            WaitUtil.sleep((int) pollIntervalMillis);
        }
        LOGGER.info(isCompilationInProgress() ? "Compilation timeout reached" : "Compilation completed");
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
}