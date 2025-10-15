package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

public class TestResultValidationComponent extends BaseComponent {

    private WebElement resultTableElement;
    private WebElement resultTableHeader;
    @Getter
    private TableComponent resultTable;
    
    // Test result status element lists
    private List<WebElement> caseErrorElementsList;
    private List<WebElement> caseSuccessElementsList;
    private List<WebElement> testResultRowElementsList;
    private WebElement testResultBadgeError;

    public TestResultValidationComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TestResultValidationComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        resultTableElement = createScopedElement("xpath=.//table[@class='table']", "resultTableElement");
        resultTableHeader = createScopedElement("xpath=.//table[@class='table']/thead//tr", "resultTableHeader");
        resultTable = createScopedComponent(TableComponent.class, "xpath=.//table[@class='table']", "resultTable");
        
        // Test result status element lists
        caseErrorElementsList = createScopedElementList("xpath=.//tr//span[@class='case-error']", "caseErrorElements");
        caseSuccessElementsList = createScopedElementList("xpath=.//tr//span[@class='case-success']", "caseSuccessElements");
        testResultRowElementsList = createScopedElementList("xpath=.//table[@class='table']//tr[contains(@class, 'test-result-row')]//td[contains(@class, 'test-name')]", "testResultRowElements");
        testResultBadgeError = createScopedElement("xpath=.//span[@class='badge badge-error']", "testResultBadgeError");
    }

    public boolean isTestTableFailed() {
        WaitUtil.isListNotEmpty(() -> caseErrorElementsList, 3000, 100, "Checking if test table has error elements");
        return testResultBadgeError.isVisible();
    }

    public boolean isTestTablePassed() {
        WaitUtil.isListNotEmpty(() -> caseSuccessElementsList, 3000, 100, "Checking if test table has success elements");
        return !testResultBadgeError.isVisible();
    }

    public int getFailedTestCount() {
        WaitUtil.waitForListNotEmpty(() -> caseErrorElementsList, 3000, 100, "Waiting for failed test elements to be available");
        return caseErrorElementsList.size();
    }

    public int getPassedTestCount() {
        WaitUtil.waitForListNotEmpty(() -> caseSuccessElementsList, 3000, 100, "Waiting for passed test elements to be available");
        return caseSuccessElementsList.size();
    }

    public int getTotalTestCount() {
        return testResultRowElementsList.size();
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