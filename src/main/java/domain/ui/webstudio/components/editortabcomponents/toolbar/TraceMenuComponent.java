package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

import java.util.List;

/**
 * The Trace dropdown of the table toolbar. Scoped under {@code form#inputArgsForm}, same as
 * {@link RunMenuComponent} — the Trace launcher shares the input-parameters form.
 */
public class TraceMenuComponent extends BaseComponent implements ITraceMenu {

    private final WebElement traceInsideMenuBtn;
    private final WebElement traceIntoFileBtn;
    private final WebElement factorTextFieldForTrace;
    private final WebElement jsonRadioBtn;
    private final WebElement jsonTextField;
    private final WebElement selectTypeDropdown;

    public TraceMenuComponent(Page page) {
        this(new WebElement(page, "xpath=//form[@id='inputArgsForm']", "inputArgsForm"));
    }

    public TraceMenuComponent(WebElement rootLocator) {
        super(rootLocator);
        traceInsideMenuBtn = createScopedElement("xpath=.//input[@id='inputArgsForm:traceButton']", "traceInsideMenuBtn");
        traceIntoFileBtn = createScopedElement("xpath=.//input[@id='inputArgsForm:traceIntoFileButton']", "traceIntoFileBtn");
        factorTextFieldForTrace = createScopedElement("xpath=.//span[text()='factor = ']/input", "factorTextFieldForTrace");
        jsonRadioBtn = createScopedElement("xpath=.//input[@type='radio' and@value='TEXT']", "jsonRadioBtn");
        jsonTextField = createScopedElement("xpath=.//textarea[contains(@id, 'jsonInput')]", "jsonTextField");
        selectTypeDropdown = createScopedElement("xpath=.//div[contains(@id, 'input')]//select", "selectTypeDropdown");
    }

    @Override
    public ITraceMenu setFactorTextField(String text) {
        if (factorTextFieldForTrace.isVisible()) {
            factorTextFieldForTrace.fill(text);
        }
        return this;
    }

    @Override
    public ITraceMenu selectJSONTrace(String json) {
        jsonRadioBtn.click();
        jsonTextField.fill(json);
        return this;
    }

    @Override
    public ITraceMenu clickTraceIntoFile() {
        traceIntoFileBtn.click();
        return this;
    }

    @Override
    public ITraceWindow clickTraceInsideMenu() {
        return clickTraceInsideMenu(true);
    }

    @Override
    public ITraceWindow clickTraceInsideMenu(boolean isPopupExpected) {
        traceInsideMenuBtn.waitForVisible();
        if (isPopupExpected) {
            // EPBDS-15551 made the trace popup chain async: click → JSF Ajax `fetchParamsForTrace`
            // (execute="@form") → /web/projects/{id} fetch → CustomEvent → React window.open.
            // On loaded CI agents this can exceed the default 10s timeout — match the 60s
            // already used by clickTraceExpectTraceWindow.
            boolean switchSet = AdvancedTracerSupport.requestAdvancedTracer(page);
            Page popup = page.waitForPopup(new Page.WaitForPopupOptions().setTimeout(60000), () -> traceInsideMenuBtn.click());
            popup.waitForLoadState();
            popup.waitForSelector("xpath=//div[@id='trace-view']", new Page.WaitForSelectorOptions().setTimeout(10000));
            if (!switchSet) {
                AdvancedTracerSupport.reopenInAdvancedTracer(popup);
            }
            return new TraceWindowComponent(popup);
        } else {
            traceInsideMenuBtn.click();
            return null;
        }
    }

    @Override
    public List<String> getAliasDropdownValues() {
        return selectTypeDropdown.getSelectValues();
    }
}
