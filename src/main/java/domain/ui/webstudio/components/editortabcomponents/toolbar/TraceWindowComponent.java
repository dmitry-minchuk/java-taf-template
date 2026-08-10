package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

import java.util.List;

/**
 * The reworked step-debugger Trace window (EPBDS-16195). Lives in its own popup {@link Page}, so every
 * element is scoped to that page. Selectors use the stable data-testid attributes of the new debugger UI.
 */
public class TraceWindowComponent extends BasePage implements ITraceWindow {

    private static final String TREE_NODE = "//*[@data-testid='trace-tree']//div[starts-with(@data-testid,'tree-frame-') or starts-with(@data-testid,'tree-step-')]";

    private final List<WebElement> callTreeNodes;
    private final WebElement callTreeNodeTemplate;
    private final WebElement status;
    private final WebElement tracedTable;
    private final WebElement details;
    private final WebElement nodeDetailsError;
    private final WebElement resumeBtn;
    private final WebElement stepOverBtn;

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
    public void close() {
        if (getPage() != null && !getPage().isClosed()) {
            LOGGER.debug("Closing trace popup window");
            getPage().close();
        }
    }
}
