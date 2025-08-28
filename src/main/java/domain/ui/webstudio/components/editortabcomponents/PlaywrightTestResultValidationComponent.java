package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightTableComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

import java.util.List;

public class PlaywrightTestResultValidationComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement resultTableElement;
    private PlaywrightWebElement resultTableHeader;
    @Getter
    private PlaywrightTableComponent resultTable;

    public PlaywrightTestResultValidationComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTestResultValidationComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        resultTableElement = createScopedElement("xpath=.//table[@class='table']", "resultTableElement");
        resultTableHeader = createScopedElement("xpath=.//table[@class='table']//tr[1]", "resultTableHeader");
        resultTable = createScopedComponent(PlaywrightTableComponent.class, "xpath=.//table[@class='table']", "resultTable");
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
        return resultTableElement.isVisible();
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

    public List<String> getTestResult(int rowIndex) {
        return getResultTable().getRow(rowIndex).getValue();
    }

    public PlaywrightTableComponent.PlaywrightTableRow getTestResultRow(int rowIndex) {
        return getResultTable().getRow(rowIndex);
    }
}