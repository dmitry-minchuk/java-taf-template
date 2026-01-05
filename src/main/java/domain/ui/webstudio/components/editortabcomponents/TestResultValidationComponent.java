package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TestResultValidationComponent extends BaseComponent {

    private WebElement resultTableHeader;
    private TableComponent resultTable;
    
    // Test result status element lists
    private List<WebElement> caseErrorElementsList;
    private List<WebElement> caseSuccessElementsList;
    private List<WebElement> testResultRowElementsList;
    private List<WebElement> testResultRowElementsLinksList;
    private List<WebElement> testResultBadgeErrors;

    public TestResultValidationComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TestResultValidationComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        resultTableHeader = createScopedElement("xpath=.//table[@class='table']/thead//tr", "resultTableHeader");
        resultTable = createScopedComponent(TableComponent.class, "xpath=.//table[@class='table']", "resultTable");
        
        // Test result status element lists
        caseErrorElementsList = createScopedElementList("xpath=.//tr//span[@class='case-error']", "caseErrorElements");
        caseSuccessElementsList = createScopedElementList("xpath=.//tr//span[@class='case-success']", "caseSuccessElements");
        testResultRowElementsList = createScopedElementList("xpath=.//table[@class='table']//tr[contains(@class, 'test-result-row')]//td[contains(@class, 'test-name')]", "testResultRowElements");
        testResultRowElementsLinksList = createScopedElementList("xpath=.//a[@class='testError']", "testResultRowElementsLinksList");
        testResultBadgeErrors = createScopedElementList("xpath=.//span[@class='badge badge-error']", "testResultBadgeError");
    }

    public TableComponent getResultTable() {
        WaitUtil.waitForCondition(() -> resultTable.isVisible() && !resultTable.getRows().isEmpty(), 5000, 50, "Waiting for resultTable to be visible");
        return resultTable;
    }

    public boolean isTestTableFailed() {
        WaitUtil.isListNotEmpty(() -> caseErrorElementsList, 3000, 100, "Checking if test table has error elements");
        return testResultBadgeErrors.stream().anyMatch(WebElement::isVisible);
    }

    public boolean isTestTablePassed() {
        WaitUtil.isListNotEmpty(() -> caseSuccessElementsList, 3000, 100, "Checking if test table has success elements");
        return testResultBadgeErrors.stream().noneMatch(WebElement::isVisible);
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

    public List<String> getAllFailedTests() {
        WaitUtil.waitForCondition(() -> !testResultRowElementsList.isEmpty(), 3000, 100, "Waiting for test result rows");
        List<String> failedTests = new ArrayList<>();
        if(!testResultRowElementsLinksList.isEmpty())
            failedTests.addAll(testResultRowElementsLinksList.stream().map(e -> e.getText().trim()).toList());

        if(!testResultRowElementsList.isEmpty()) {
            for (int i = 1; i <= getTotalTestCount(); i++) {
                List<String> rowData = getResultTable().getRow(i).getValue();
                if (!rowData.isEmpty()) {
                    // Check if this row has error badge using the badge list
                    String rowXpath = String.format(".//table[@class='table']//tr[contains(@class, 'test-result-row')][%d]//span[@class='badge badge-error']", i);
                    WebElement errorBadge = createScopedElement("xpath=" + rowXpath, "errorBadge");

                    try {
                        if (errorBadge.isVisible(500)) {
                            // This row has failed tests - get all details from the row
                            String failureDetails = String.join(" | ", rowData);
                            failedTests.add(failureDetails);
                        }
                    } catch (Exception e) {
                        // No error badge in this row, skip it
                    }
                }
            }
        }

        return failedTests;
    }
}