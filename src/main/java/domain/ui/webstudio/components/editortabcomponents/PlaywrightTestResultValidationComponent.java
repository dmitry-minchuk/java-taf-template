package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

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
        resultTable = new PlaywrightWebElement(page, ".//table[@class='table']", "Result Table");
        resultTableHeader = new PlaywrightWebElement(page, ".//table[@class='table']//tr[1]", "Result Table Header");
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
}