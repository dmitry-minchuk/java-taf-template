package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightTraceActionsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement traceTree;
    private PlaywrightWebElement traceInputArgs;
    private PlaywrightWebElement traceStart;
    private PlaywrightWebElement traceClose;
    private PlaywrightWebElement expandHandle;
    private PlaywrightWebElement numberInput;

    public PlaywrightTraceActionsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTraceActionsComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        traceTree = createScopedElement("xpath=.//div[contains(@class,'trace-tree')] | .//tree", "traceTree");
        traceInputArgs = createScopedElement("xpath=.//div[contains(@id, 'inputArgsForm')]//span[@class='rf-trn-hnd-colps rf-trn-hnd']", "traceInputArgs");
        traceStart = createScopedElement("xpath=.//button[contains(@id,'TraceStart')] | .//input[contains(@value,'Start')]", "traceStart");
        traceClose = createScopedElement("xpath=.//button[contains(text(),'Close')] | .//input[contains(@value,'Close')]", "traceClose");
        expandHandle = createScopedElement("xpath=.//span[@class='rf-trn-hnd-colps rf-trn-hnd']", "expandHandle");
        numberInput = createScopedElement("xpath=.//input[contains(@id, 'financialData.totalAssets:numberNodeValue')]", "numberInput");
    }

    public void selectTreeItem(int itemIndex) {
        PlaywrightWebElement treeItem = createScopedElement("xpath=.//tree//item[" + itemIndex + "] | .//div[contains(@class,'tree-item')][" + itemIndex + "]", "treeItem" + itemIndex);
        treeItem.click();
        page.waitForTimeout(500);
    }

    public void expandItemInTree(int itemIndex) {
        PlaywrightWebElement expandButton = createScopedElement("xpath=.//tree//item[" + itemIndex + "]//span[contains(@class,'expand')] | .//div[contains(@class,'tree-item')][" + itemIndex + "]//span[contains(@class,'expand')]", "expandButton" + itemIndex);
        if (expandButton.isVisible()) {
            expandButton.click();
        } else {
            // Try to expand by clicking on the item itself
            selectTreeItem(itemIndex);
        }
        page.waitForTimeout(500);
    }

    public String getItemInTreeValue(int itemIndex) {
        PlaywrightWebElement treeItem = createScopedElement("xpath=.//tree//item[" + itemIndex + "] | .//div[contains(@class,'tree-item')][" + itemIndex + "]", "treeItem" + itemIndex);
        return treeItem.getText();
    }

    public String getReturnedResult() {
        PlaywrightWebElement resultElement = createScopedElement("xpath=.//div[contains(@class,'result')] | .//span[contains(@class,'result')]", "resultElement");
        return resultElement.getText();
    }

    public void close() {
        traceClose.click();
        page.waitForTimeout(500);
    }

    public void clickInputArgsExpand() {
        traceInputArgs.click();
        page.waitForTimeout(500);
    }

    public void setFinancialDataTotalAssets(String value) {
        numberInput.fill(value);
    }

    public void startTrace() {
        traceStart.click();
        page.waitForTimeout(1000);
    }

    public boolean isTraceWindowVisible() {
        return traceTree.isVisible();
    }

    public String getTraceInfo() {
        return "Trace Actions Component - Tree and controls";
    }
}