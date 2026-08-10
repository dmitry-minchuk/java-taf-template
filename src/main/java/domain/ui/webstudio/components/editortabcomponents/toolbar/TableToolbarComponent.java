package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

/**
 * The second-line (table) toolbar of the editor: Run / Trace / Benchmark launchers with their dropdown
 * arrows, the Edit / Copy / Remove / Create Test table actions and the target-table / available-test-runs
 * sections. Scoped under {@code div#tableToolbarPanel} (verified against the live 6.4.0 DOM).
 */
public class TableToolbarComponent extends BaseComponent {

    private final WebElement runBtn;
    private final WebElement runDropdownBtn;
    private final WebElement traceBtn;
    private final WebElement traceDropdownBtn;
    private final WebElement benchmarkBtn;
    private final WebElement benchmarkDropdownBtn;
    private final WebElement editTableBtn;
    private final WebElement copyTableBtn;
    private final WebElement removeBtn;
    private final WebElement createTestBtn;
    private final WebElement targetTableLink;
    private final WebElement availableTestRunsLink;
    private final WebElement availableTestRunsInlineLink;
    private final WebElement availableTestRunsExpandLink;
    private final WebElement availableTestRunsPopup;
    private final WebElement tableActionsTestBtn;
    private final WebElement tableActionsTestDropdownBtn;
    // These two live inside the run/test dropdowns of this toolbar; they are located by their unique ids
    // at page level because the menus only exist in the DOM while open.
    private final WebElement withinCurrentModuleOnlyInputArgs;
    private final WebElement withinCurrentModuleOnlyTestTables;

    public TableToolbarComponent(Page page) {
        this(new WebElement(page, "xpath=//div[@id='tableToolbarPanel']", "tableToolbarPanel"));
    }

    public TableToolbarComponent(WebElement rootLocator) {
        super(rootLocator);
        runBtn = createScopedElement("xpath=.//img[contains(@src, 'run')]", "runBtn");
        runDropdownBtn = createScopedElement("xpath=.//a[@id='runLink']//td[@class='arrow']", "runDropdownBtn");
        traceBtn = createScopedElement("xpath=.//img[contains(@src, 'trace')]", "traceBtn");
        traceDropdownBtn = createScopedElement("xpath=.//a[@id='traceLink']//td[@class='arrow']", "traceDropdownBtn");
        benchmarkBtn = createScopedElement("xpath=.//span[contains(text(), 'Benchmark')]", "benchmarkBtn");
        benchmarkDropdownBtn = createScopedElement("xpath=.//a[@id='benchmarkLink']//td[@class='arrow']", "benchmarkDropdownBtn");
        editTableBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'editTable')]]", "editBtn");
        copyTableBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'copyTable')]]", "copyBtn");
        removeBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./span[@class='delete-icon']]", "removeBtn");
        createTestBtn = createScopedElement("xpath=.//a[@title='Create a new Test table']", "createTestBtn");
        targetTableLink = createScopedElement("xpath=.//section[@id='targetTablesSection']//a", "targetTableLink");
        availableTestRunsLink = createScopedElement("xpath=.//section[@id='testsSection']", "availableTestRunsLink");
        availableTestRunsInlineLink = createScopedElement("xpath=.//section[@id='testsSection']//span//a[not(@title='Other Available Tests/Runs')]", "availableTestRunsInlineLink");
        availableTestRunsExpandLink = createScopedElement("xpath=.//section[@id='testsSection']//a[@title='Other Available Tests/Runs']", "availableTestRunsExpandLink");
        availableTestRunsPopup = createScopedElement("xpath=.//*[@id='tests-section-available-tests-id']", "availableTestRunsPopup");
        tableActionsTestBtn = createScopedElement("xpath=.//span[text()='Test']", "tableActionsTestBtn");
        tableActionsTestDropdownBtn = createScopedElement("xpath=.//a[.//span[text()='Test']]//td[@class='arrow']", "tableActionsTestDropdownBtn");
        withinCurrentModuleOnlyInputArgs = new WebElement(page, "xpath=//input[@id='runTestModuleOnlyInputArgs']", "withinCurrentModuleOnlyInputArgs");
        withinCurrentModuleOnlyTestTables = new WebElement(page, "xpath=//input[@id='runTestModuleOnly']", "withinCurrentModuleOnlyTestTables");
    }

    // ========== Launchers ==========

    public IRunMenu clickRun() {
        runBtn.waitForVisible();
        runBtn.click();
        return new RunMenuComponent(page);
    }

    public ITraceMenu clickTrace() {
        traceBtn.click();
        return new TraceMenuComponent(page);
    }

    public ITraceWindow clickTraceExpectTraceWindow() {
        waitUntilSpinnerLoaded();
        traceBtn.waitForVisible();

        // Retry the click+wait: on slow CI the first click can land before the rule is runnable, so no popup opens.
        boolean switchSet = AdvancedTracerSupport.requestAdvancedTracer(page);
        Page popup = WaitUtil.retryOnException(
                () -> page.waitForPopup(new Page.WaitForPopupOptions().setTimeout(30000), traceBtn::click),
                65000, 1000, "Opening Trace popup window");
        popup.waitForLoadState();
        popup.waitForSelector("xpath=//div[@id='trace-view']", new Page.WaitForSelectorOptions().setTimeout(10000));
        if (!switchSet) {
            AdvancedTracerSupport.reopenInAdvancedTracer(popup);
        }
        return new TraceWindowComponent(popup);
    }

    public void clickBenchmark() {
        benchmarkBtn.click();
    }

    public void clickRunDropdown() {
        runBtn.waitForVisible();
        runBtn.hover();
        runDropdownBtn.click();
    }

    public void clickBenchmarkDropdown() {
        benchmarkBtn.waitForVisible();
        benchmarkBtn.hover();
        benchmarkDropdownBtn.click();
    }

    public boolean isRunButtonVisible() {
        return runBtn.isVisible(1000);
    }

    public boolean isTraceButtonVisible() {
        return traceBtn.isVisible(1000);
    }

    public boolean isBenchmarkButtonVisible() {
        return benchmarkBtn.isVisible(1000);
    }

    // ========== Table actions ==========

    public WebElement getEditTableBtn() {
        return editTableBtn;
    }

    public WebElement getCopyTableBtn() {
        return copyTableBtn;
    }

    public WebElement getRemoveBtn() {
        return removeBtn;
    }

    public WebElement getCreateTestBtn() {
        return createTestBtn;
    }

    public void clickTableActionsTestBtn() {
        tableActionsTestBtn.click();
    }

    public void clickTableActionsTestDropdown() {
        tableActionsTestBtn.waitForVisible();
        tableActionsTestBtn.hover();
        tableActionsTestDropdownBtn.click();
    }

    // ========== Target table and available test runs ==========

    public String getTargetTableText() {
        if (targetTableLink.isVisible(2000)) {
            return targetTableLink.getText().trim();
        }
        return "";
    }

    public boolean isTargetTableVisible() {
        return targetTableLink.isVisible(2000);
    }

    public void clickTargetTable() {
        targetTableLink.click();
        WaitUtil.sleep(500, "Waiting for target table navigation");
    }

    public String getAvailableTestRunsLinkText() {
        if (availableTestRunsLink.isVisible(2000)) {
            return availableTestRunsLink.getText().trim();
        }
        return "";
    }

    public boolean isAvailableTestRunsLinkVisible() {
        return availableTestRunsLink.isVisible(1000);
    }

    public String getAvailableTestRunsInlineLinkText() {
        return availableTestRunsInlineLink.getText().trim();
    }

    public void clickAvailableTestRunsInlineLink() {
        availableTestRunsInlineLink.click();
        WaitUtil.sleep(500, "Waiting for navigation to Test/Run table");
    }

    public boolean isAvailableTestRunsExpandLinkVisible() {
        return availableTestRunsExpandLink.isVisible(1000);
    }

    public void clickAvailableTestRunsExpandLink() {
        availableTestRunsExpandLink.click();
        WaitUtil.sleep(300, "Waiting for popup with all Tests/Runs to appear");
    }

    public String getAvailableTestRunsPopupText() {
        return availableTestRunsPopup.getText().trim().replaceAll("\\s*\\n\\s*", "\n");
    }

    // ========== Within Current Module Only (run/test dropdowns) ==========

    public boolean isWithinCurrentModuleOnlyInputArgsChecked() {
        return withinCurrentModuleOnlyInputArgs.isChecked();
    }

    public boolean isWithinCurrentModuleOnlyInputArgsEnabled() {
        return withinCurrentModuleOnlyInputArgs.isEnabled();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesChecked() {
        return withinCurrentModuleOnlyTestTables.isChecked();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesEnabled() {
        return withinCurrentModuleOnlyTestTables.isEnabled();
    }
}
