package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightTableComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

import java.util.List;

public class PlaywrightTestResultValidationComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement resultTable;
    private PlaywrightWebElement resultTableHeader;

    public PlaywrightTestResultValidationComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTestResultValidationComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        resultTable = createScopedElement(".//table[@class='table']", "resultTable");
        resultTableHeader = createScopedElement(".//table[@class='table']//tr[1]", "resultTableHeader");
    }

    public boolean isTestTableFailed() {
        return page.locator(".//tr//span[@class='case-error']").count() > 0;
    }

    public boolean isTestTablePassed() {
        return page.locator(".//tr//span[@class='case-success']").count() > 0;
    }

    public int getFailedTestCount() {
        return page.locator(".//tr//span[@class='case-error']").count();
    }

    public int getPassedTestCount() {
        return page.locator(".//tr//span[@class='case-success']").count();
    }

    public int getTotalTestCount() {
        return page.locator(".//table[@class='table']//tr[contains(@class, 'test-result-row')]//td[contains(@class, 'test-name')]").count();
    }

    public boolean isResultTableVisible() {
        return resultTable.isVisible();
    }

    public boolean isResultTableHeaderVisible() {
        return resultTableHeader.isVisible();
    }

    public String getTestResultSummary() {
        int total = getTotalTestCount();
        int passed = getPassedTestCount();
        int failed = getFailedTestCount();
        return String.format("Total: %d, Passed: %d, Failed: %d", total, passed, failed);
    }
    
    // Additional methods for compatibility with legacy tests
    public boolean isResultTablePresent() {
        return isResultTableVisible();
    }
    
    public String getResultTableHeader() {
        if (resultTableHeader.isVisible()) {
            return resultTableHeader.getText();
        }
        return "";
    }
    
    /**
     * Get test result table as PlaywrightTableComponent for row operations
     * @return PlaywrightTableComponent for the result table
     */
    public PlaywrightTableComponent getResultTable() {
        return new PlaywrightTableComponent(page, "xpath=//table[@class='table']");
    }
    
    /**
     * Get test result data for specific row
     * @param rowIndex 1-based row index
     * @return List of cell values for the specified row
     */
    public List<String> getTestResultData(int rowIndex) {
        return getResultTable().getRow(rowIndex).getValue();
    }
    
    /**
     * Get test result row as PlaywrightTableComponent.PlaywrightTableRow for direct access
     * @param rowIndex 1-based row index
     * @return PlaywrightTableRow for the specified row
     */
    public PlaywrightTableComponent.PlaywrightTableRow getTestResultRow(int rowIndex) {
        return getResultTable().getRow(rowIndex);
    }
}