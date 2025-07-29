package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

@Getter

public class PlaywrightTableToolbarPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement toolbar;
    private PlaywrightWebElement runBtn;
    private PlaywrightWebElement traceBtn;
    private PlaywrightWebElement benchmarkBtn;
    private PlaywrightWebElement exportBtn;
    private PlaywrightWebElement editBtn;

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