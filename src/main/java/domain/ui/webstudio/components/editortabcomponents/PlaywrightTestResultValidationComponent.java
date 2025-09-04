package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightTableComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

import java.util.List;

public class PlaywrightTestResultValidationComponent extends CoreComponent {

    private PlaywrightWebElement resultTableElement;
    private PlaywrightWebElement resultTableHeader;
    @Getter
    private PlaywrightTableComponent resultTable;
    
    // Test result status element lists
    private List<PlaywrightWebElement> caseErrorElementsList;
    private List<PlaywrightWebElement> caseSuccessElementsList;
    private List<PlaywrightWebElement> testResultRowElementsList;

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
        resultTableHeader = createScopedElement("xpath=.//table[@class='table']/thead//tr", "resultTableHeader");
        resultTable = createScopedComponent(PlaywrightTableComponent.class, "xpath=.//table[@class='table']", "resultTable");
        
        // Test result status element lists
        caseErrorElementsList = createScopedElementList("xpath=.//tr//span[@class='case-error']", "caseErrorElements");
        caseSuccessElementsList = createScopedElementList("xpath=.//tr//span[@class='case-success']", "caseSuccessElements");
        testResultRowElementsList = createScopedElementList("xpath=.//table[@class='table']//tr[contains(@class, 'test-result-row')]//td[contains(@class, 'test-name')]", "testResultRowElements");
    }

    public boolean isTestTableFailed() {
        isResultTableVisible();
        return !caseErrorElementsList.isEmpty();
    }

    public boolean isTestTablePassed() {
        isResultTableVisible();
        return !caseSuccessElementsList.isEmpty();
    }

    public int getFailedTestCount() {
        isResultTableVisible();
        return caseErrorElementsList.size();
    }

    public int getPassedTestCount() {
        isResultTableVisible();
        return caseSuccessElementsList.size();
    }

    public int getTotalTestCount() {
        return testResultRowElementsList.size();
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
    
    public String getResultTableHeader() {
        if (resultTableHeader.isVisible()) {
            return resultTableHeader.getText();
        }
        return "";
    }

    public List<String> getTestResult(int rowIndex) {
        return getResultTable().getRow(rowIndex).getValue();
    }
}