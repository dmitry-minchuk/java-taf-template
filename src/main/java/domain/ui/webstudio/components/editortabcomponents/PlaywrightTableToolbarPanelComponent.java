package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import java.util.List;
import java.util.ArrayList;

@Getter

public class PlaywrightTableToolbarPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement toolbar;
    private PlaywrightWebElement runBtn;
    private PlaywrightWebElement traceBtn;
    private PlaywrightWebElement benchmarkBtn;
    private PlaywrightWebElement exportBtn;
    private PlaywrightWebElement editBtn;
    private PlaywrightWebElement factorTextField;
    private PlaywrightWebElement traceDropdownBtn;

    public PlaywrightTableToolbarPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTableToolbarPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        toolbar = createScopedElement("xpath=.//div[contains(@class,'table-toolbar')]", "toolbar");
        runBtn = createScopedElement("xpath=.//button[contains(@title,'Run') or contains(text(),'Run')]", "runBtn");
        traceBtn = createScopedElement("xpath=.//button[contains(@title,'Trace') or contains(text(),'Trace')]", "traceBtn");
        benchmarkBtn = createScopedElement("xpath=.//button[contains(@title,'Benchmark') or contains(text(),'Benchmark')]", "benchmarkBtn");
        exportBtn = createScopedElement("xpath=.//button[contains(@title,'Export') or contains(text(),'Export')]", "exportBtn");
        editBtn = createScopedElement("xpath=.//a[.//div[text()='Edit'] or .//tr/td/span[text()='Edit']]", "editBtn");
        factorTextField = createScopedElement("xpath=.//input[@type='text' and contains(@placeholder,'Factor') or @id='factorInput']", "factorTextField");
        traceDropdownBtn = createScopedElement("xpath=.//button[contains(@title,'Trace') or .//div[text()='Trace']]", "traceDropdownBtn");
    }

    public void clickRun() {
        runBtn.click();
    }

    public void clickTrace() {
        traceBtn.click();
    }

    public void clickBenchmark() {
        benchmarkBtn.click();
    }

    public void clickExport() {
        exportBtn.click();
    }

    public boolean isToolbarVisible() {
        return toolbar.isVisible();
    }

    public boolean isRunButtonEnabled() {
        return runBtn.isEnabled();
    }

    public boolean isTraceButtonEnabled() {
        return traceBtn.isEnabled();
    }

    public boolean isBenchmarkButtonEnabled() {
        return benchmarkBtn.isEnabled();
    }

    public boolean isExportButtonEnabled() {
        return exportBtn.isEnabled();
    }

    public PlaywrightTraceMenu setFactorTextField(String text) {
        factorTextField.fill(text);
        return new PlaywrightTraceMenu(this);
    }

    public PlaywrightTraceWindow clickTraceInsideMenu() {
        traceDropdownBtn.click();
        // Wait for trace window to open - in Playwright we don't need window switching like Selenium
        page.waitForSelector("xpath=.//div[contains(@class,'trace-window') or contains(@title,'Trace')]", 
                            new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
        return new PlaywrightTraceWindow();
    }

    // Inner TraceMenu class for Playwright
    public static class PlaywrightTraceMenu {
        private PlaywrightTableToolbarPanelComponent parent;

        public PlaywrightTraceMenu(PlaywrightTableToolbarPanelComponent parent) {
            this.parent = parent;
        }

        public PlaywrightTraceMenu setFactorTextField(String text) {
            return parent.setFactorTextField(text);
        }

        public PlaywrightTraceWindow clickTraceInsideMenu() {
            return parent.clickTraceInsideMenu();
        }
    }

    // Inner TraceWindow class for Playwright
    public static class PlaywrightTraceWindow {
        private PlaywrightWebElement traceTree;

        public PlaywrightTraceWindow() {
            // Initialize trace window elements
            traceTree = new PlaywrightWebElement(PlaywrightDriverPool.getPage(), 
                "xpath=.//div[contains(@class,'trace-tree') or contains(@id,'traceTree')]", "traceTree");
        }

        public PlaywrightTraceWindow expandItemInTree(int position) {
            PlaywrightWebElement item = new PlaywrightWebElement(PlaywrightDriverPool.getPage(),
                String.format("xpath=(.//div[contains(@class,'trace-item')][%d]//span[contains(@class,'expand')])[1]", position + 1), 
                "expandItem" + position);
            item.click();
            return this;
        }

        public List<String> getVisibleItemsFromTree() {
            List<String> items = new ArrayList<>();
            // Get all visible trace items
            var page = PlaywrightDriverPool.getPage();
            var locators = page.locator("xpath=.//div[contains(@class,'trace-item') or contains(@class,'trace-node')]//span[contains(@class,'trace-text')]");
            int count = locators.count();
            for (int i = 0; i < count; i++) {
                String text = locators.nth(i).textContent();
                if (text != null && !text.trim().isEmpty()) {
                    items.add(text.trim());
                }
            }
            return items;
        }
    }
}