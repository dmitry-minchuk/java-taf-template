package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TraceWindowComponent extends BasePage implements ITraceWindow {

    private static final Logger LOGGER = LogManager.getLogger(TraceWindowComponent.class);

    private static final String TREE_NODE = "//*[@data-testid='trace-tree']//div[starts-with(@data-testid,'tree-frame-') or starts-with(@data-testid,'tree-step-')]";

    private final List<WebElement> callTreeNodes;
    private final WebElement callTreeNodeTemplate;
    private final WebElement status;
    private final WebElement tracedTable;
    private final WebElement details;
    private final WebElement nodeDetailsError;
    private final WebElement resumeBtn;
    private final WebElement stepOverBtn;
    private final WebElement debugToolbar;
    private final WebElement detailedTraceToggle;
    private final WebElement simpleTree;
    private final WebElement breakOnFireCheckbox;
    private final WebElement breakOnRuleSelect;
    private final WebElement breakOnRuleOption;
    private final WebElement watchInput;
    private final WebElement watchAddButton;
    private final WebElement watchPanel;
    private final WebElement rerunBtn;
    private WebElement decisionPanel;

    public TraceWindowComponent(Page tracePage) {
        super(tracePage);
        callTreeNodes = createElementList("xpath=" + TREE_NODE, "callTreeNodes");
        callTreeNodeTemplate = new WebElement(tracePage, "xpath=(" + TREE_NODE + ")[%d]", "callTreeNode");
        status = new WebElement(tracePage, "xpath=//*[@data-testid='debug-status']", "debugStatus");
        tracedTable = new WebElement(tracePage, "xpath=//*[@data-testid='trace-table']", "tracedTable");
        details = new WebElement(tracePage, "xpath=//*[@data-testid='debug-details']", "debugDetails");
        nodeDetailsError = new WebElement(tracePage, "xpath=//*[@data-testid='debug-details' and contains(.,'Failed to load node details')]", "nodeDetailsError");
        resumeBtn = new WebElement(tracePage, "xpath=//*[@data-testid='debug-resume']", "resumeBtn");
        stepOverBtn = new WebElement(tracePage, "xpath=//*[@data-testid='debug-step-over']", "stepOverBtn");
        debugToolbar = new WebElement(tracePage, "xpath=//*[@data-testid='debug-toolbar']", "debugToolbar");
        detailedTraceToggle = new WebElement(tracePage, "xpath=//*[@data-testid='trace-detailed']", "detailedTraceToggle");
        simpleTree = new WebElement(tracePage, "xpath=//*[@data-testid='simple-tree']", "simpleTree");
        breakOnFireCheckbox = new WebElement(tracePage,
                "css=[data-testid=decision-break-on-fire] input, input[data-testid=decision-break-on-fire]",
                "breakOnFireCheckbox");
        breakOnRuleSelect = new WebElement(tracePage, "xpath=//*[@data-testid='decision-rule-select']", "breakOnRuleSelect");
        breakOnRuleOption = new WebElement(tracePage,
                "xpath=//div[contains(@class,'ant-select-dropdown')][not(contains(@class,'ant-select-dropdown-hidden'))]//div[contains(@class,'ant-select-item-option')][@title='%s' or .//*[normalize-space()='%s']]",
                "breakOnRuleOption");
        watchInput = new WebElement(tracePage,
                "css=[data-testid=watch-add] input, input[data-testid=watch-add]", "watchInput");
        watchAddButton = new WebElement(tracePage, "xpath=//*[@data-testid='watch-add-button']", "watchAddButton");
        watchPanel = new WebElement(tracePage, "xpath=//*[@data-testid='watch-panel']", "watchPanel");
        rerunBtn = new WebElement(tracePage, "xpath=//*[@data-testid='debug-rerun']", "rerunBtn");
        decisionPanel = new WebElement(tracePage, "xpath=//*[@data-testid='decision-panel']", "decisionPanel");
    }

    @Override
    public String getStatus() {
        return status.getText().trim();
    }

    @Override
    public List<String> getCallTreeTitles() {
        WaitUtil.waitForCondition(() -> !callTreeNodes.isEmpty(), 10000, 200, "Waiting for trace call-tree nodes to appear");
        return callTreeNodes.stream().map(n -> n.getText().replaceAll("\\s+", " ").trim()).toList();
    }

    @Override
    public ITraceWindow selectTreeNode(int position) {
        callTreeNodeTemplate.format(position + 1).click();
        WaitUtil.waitForCondition(() -> details.isVisible(1000), 5000, 200, "Waiting for node details to load");
        return this;
    }

    @Override
    public ITraceWindow stepOver() {
        stepOverBtn.click();
        return this;
    }

    @Override
    public ITraceWindow resume() {
        resumeBtn.click();
        return this;
    }

    @Override
    public String getDetailsText() {
        return details.getText();
    }

    @Override
    public String getTracedTableText() {
        return tracedTable.getText();
    }

    @Override
    public boolean isNodeDetailsErrorDisplayed(int timeoutInMillis) {
        return nodeDetailsError.isVisible(timeoutInMillis);
    }

    @Override
    public boolean areDetailsDisplayed(int timeoutInMillis) {
        return details.isVisible(timeoutInMillis);
    }

    @Override
    public boolean isDebugToolbarShown(int timeoutInMillis) {
        return debugToolbar.isVisible(timeoutInMillis);
    }

    @Override
    public boolean isBusinessToggleShown(int timeoutInMillis) {
        return detailedTraceToggle.isVisible(timeoutInMillis);
    }

    @Override
    public ITraceWindow toggleDetailedTrace() {
        detailedTraceToggle.click();
        return this;
    }

    @Override
    public String getSimpleTreeText() {
        return simpleTree.waitForVisible(DEFAULT_TIMEOUT_MS).getText().replaceAll("\\s+", " ").trim();
    }

    @Override
    public ITraceWindow setBreakWhenRuleFires() {
        breakOnFireCheckbox.checkEvenIfHidden();
        return this;
    }

    @Override
    public ITraceWindow pickBreakOnRule(String ruleName) {
        breakOnRuleSelect.click();
        breakOnRuleOption.format(ruleName, ruleName).waitForVisible(DEFAULT_TIMEOUT_MS).click();
        page.keyboard().press("Escape");
        return this;
    }

    @Override
    public ITraceWindow addWatch(String expression) {
        watchInput.fill(expression);
        watchAddButton.click();
        return this;
    }

    @Override
    public String getWatchPanelText() {
        return watchPanel.getText().replaceAll("\\s+", " ").trim();
    }

    @Override
    public ITraceWindow clickRerun() {
        rerunBtn.click();
        return this;
    }

    @Override
    public ITraceWindow waitForStatus(String expectedStatus, int timeoutInMillis) {
        WaitUtil.waitForCondition(() -> getStatus().contains(expectedStatus), timeoutInMillis, 250,
                "Waiting for the debugger status '" + expectedStatus + "'");
        return this;
    }

    @Override
    public String getDecisionPanelText() {
        return decisionPanel.getText().replaceAll("\\s+", " ").trim();
    }

    @Override
    public ITraceWindow waitForDecisionPanelToContain(String expectedText, int timeoutInMillis) {
        WaitUtil.waitForCondition(() -> getDecisionPanelText().contains(expectedText), timeoutInMillis, 250,
                "Waiting for the decision panel to report '" + expectedText + "'");
        return this;
    }

    @Override
    public void close() {
        if (getPage() != null && !getPage().isClosed()) {
            LOGGER.debug("Closing trace popup window");
            getPage().close();
        }
    }
}
