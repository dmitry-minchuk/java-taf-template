package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

import java.util.ArrayList;
import java.util.List;

public class PlaywrightProblemsPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement showProblemsLink;
    private PlaywrightWebElement hideProblemPanelLink;
    private PlaywrightWebElement errorsCounter;
    private PlaywrightWebElement warningsCounter;
    private PlaywrightWebElement compilationProgressBar;
    private List<PlaywrightWebElement> errorElements;
    private List<PlaywrightWebElement> warningElements;

    public PlaywrightProblemsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightProblemsPanelComponent(PlaywrightWebElement rootLocator) {
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
        String compilationStatus = compilationProgressBar.getText();
        String compilationComplete = "Loaded 100%";
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
                .map(PlaywrightWebElement::getText)
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
        
        List<PlaywrightWebElement> allProblems = new ArrayList<>();
        allProblems.addAll(errorElements);
        allProblems.addAll(warningElements);
        
        allProblems.stream()
                .filter(element -> element.getText().contains(text))
                .findFirst()
                .ifPresent(PlaywrightWebElement::click);
    }

    public void selectProblemByIndex(int index) {
        showProblemsPanel();
        waitForCompilationToComplete();
        
        if (index > 0 && index <= errorElements.size()) {
            errorElements.get(index - 1).click();
        }
    }
}