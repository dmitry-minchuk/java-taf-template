package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightTableToolbarPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement toolbar;
    private PlaywrightWebElement runBtn;
    private PlaywrightWebElement traceBtn;
    private PlaywrightWebElement benchmarkBtn;
    private PlaywrightWebElement exportBtn;

    public PlaywrightTableToolbarPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTableToolbarPanelComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        toolbar = new PlaywrightWebElement(page, ".//div[contains(@class,'table-toolbar')]", "Table Toolbar");
        runBtn = new PlaywrightWebElement(page, ".//button[contains(@title,'Run') or contains(text(),'Run')]", "Run Button");
        traceBtn = new PlaywrightWebElement(page, ".//button[contains(@title,'Trace') or contains(text(),'Trace')]", "Trace Button");
        benchmarkBtn = new PlaywrightWebElement(page, ".//button[contains(@title,'Benchmark') or contains(text(),'Benchmark')]", "Benchmark Button");
        exportBtn = new PlaywrightWebElement(page, ".//button[contains(@title,'Export') or contains(text(),'Export')]", "Export Button");
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
}