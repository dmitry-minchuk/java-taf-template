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
        showProblemsLink = createScopedElement(".//div[@class='ui-layout-toggler ui-layout-toggler-south ui-layout-toggler-closed ui-layout-toggler-south-closed' and @title='Open']", "showProblemsLink");
        hideProblemPanelLink = createScopedElement(".//div[@id='bottom']//span[@id='south-closer']", "hideProblemPanelLink");
        errorsCounter = createScopedElement("#errors-count", "errorsCounter");
        warningsCounter = createScopedElement("#warnings-count", "warningsCounter");
        compilationProgressBar = createScopedElement(".//div[@class='panel']//div[@id='progress-info-panel']", "compilationProgressBar");
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