package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestResultValidationComponent extends BasePageComponent {

    @FindBy(xpath = ".//table[@class='table']//tr[contains(@class, 'test-result-row')]//td[contains(@class, 'test-name')]")
    private List<SmartWebElement> testNameCells;

    @FindBy(xpath = ".//tr//span[@class='case-error']")
    private List<SmartWebElement> failedTestCells;

    @FindBy(xpath = ".//tr//span[@class='case-success']")
    private List<SmartWebElement> passedTestCells;

    public boolean isTestTableFailed() {
        return !failedTestCells.isEmpty();
    }

    public boolean isTestTablePassed() {
        return failedTestCells.isEmpty();
    }

    public void checkTestTableFailed(String testName) {
        assertThat(isTestTableFailed())
                .as("Test table '%s' should have failed status", testName)
                .isTrue();
    }

    public void checkTestTablePassed(String testName) {
        assertThat(isTestTablePassed())
                .as("Test table '%s' should have passed status", testName)
                .isTrue();
    }

    public List<String> getAllTestNames() {
        return testNameCells.stream()
                .map(SmartWebElement::getText)
                .toList();
    }

    public void checkTestTablePresent(String testName, boolean isPresent) {
        boolean found = getAllTestNames().contains(testName);
        assertThat(found)
                .as("Test table '%s' should %s in results", testName, isPresent ? "be present" : "not be present")
                .isEqualTo(isPresent);
    }

    public SmartWebElement getTestResultRow(String testName) {
        return new SmartWebElement(getDriver(), By.xpath("//table[@class='table']//tr[contains(@class, 'test-result-row') and .//td[text()='" + testName + "']]"));
    }
}