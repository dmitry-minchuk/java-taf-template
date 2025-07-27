package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightProblemsPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement showProblemsLink;
    private PlaywrightWebElement hideProblemPanelLink;
    private PlaywrightWebElement errorsCounter;
    private PlaywrightWebElement warningsCounter;
    private PlaywrightWebElement compilationProgressBar;

    public PlaywrightProblemsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightProblemsPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        showProblemsLink = new PlaywrightWebElement(page, ".//div[@class='ui-layout-toggler ui-layout-toggler-south ui-layout-toggler-closed ui-layout-toggler-south-closed' and @title='Open']", "Show Problems Link");
        hideProblemPanelLink = new PlaywrightWebElement(page, ".//div[@id='bottom']//span[@id='south-closer']", "Hide Problem Panel Link");
        errorsCounter = new PlaywrightWebElement(page, "#errors-count", "Errors Counter");
        warningsCounter = new PlaywrightWebElement(page, "#warnings-count", "Warnings Counter");
        compilationProgressBar = new PlaywrightWebElement(page, ".//div[@class='panel']//div[@id='progress-info-panel']", "Compilation Progress Bar");
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
        String errorText = errorsCounter.textContent();
        return errorText != null && !errorText.isEmpty() ? Integer.parseInt(errorText.trim()) : 0;
    }

    public int getWarningsCount() {
        String warningText = warningsCounter.textContent();
        return warningText != null && !warningText.isEmpty() ? Integer.parseInt(warningText.trim()) : 0;
    }

    public int getAllErrorsCount() {
        return page.locator(".//div[@id='errors-panel']//a").count();
    }

    public int getAllWarningsCount() {
        return page.locator(".//div[@id='warnings-panel']//a").count();
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
}