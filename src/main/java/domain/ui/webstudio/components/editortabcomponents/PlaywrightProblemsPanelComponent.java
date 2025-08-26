package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static configuration.core.ui.factory.PlaywrightListFactory.createElementsList;

public class PlaywrightProblemsPanelComponent extends PlaywrightBasePageComponent {

    private static final Logger logger = LoggerFactory.getLogger(PlaywrightProblemsPanelComponent.class);

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
        logger.info("[DEBUG] initializeElements() called, rootLocator: {}", (rootLocator != null ? "present" : "null"));
        
        showProblemsLink = createScopedElement(".//div[@class='ui-layout-toggler ui-layout-toggler-south ui-layout-toggler-closed ui-layout-toggler-south-closed' and @title='Open']", "showProblemsLink");
        hideProblemPanelLink = createScopedElement(".//div[@id='bottom']//span[@id='south-closer']", "hideProblemPanelLink");
        errorsCounter = createScopedElement("#errors-count", "errorsCounter");
        warningsCounter = createScopedElement("#warnings-count", "warningsCounter");
        compilationProgressBar = createScopedElement(".//div[@class='panel']//div[@id='progress-info-panel']", "compilationProgressBar");
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
        String errorText = errorsCounter.getText();
        return errorText != null && !errorText.isEmpty() ? Integer.parseInt(errorText.trim()) : 0;
    }

    public int getWarningsCount() {
        String warningText = warningsCounter.getText();
        return warningText != null && !warningText.isEmpty() ? Integer.parseInt(warningText.trim()) : 0;
    }

    public boolean isCompilationInProgress() {
        return compilationProgressBar.isVisible();
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
        return errorElements.stream()
                .map(PlaywrightWebElement::getText)
                .collect(Collectors.toList());
    }
}