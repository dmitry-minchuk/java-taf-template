package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Shared logic for launching the trace in the advanced step debugger.
 *
 * <p>6.4.0 opens the trace in the simple business view unless the launcher passes {@code advanced=true},
 * which it reads from a checkbox at click time (isAdvancedTracer in table.xhtml). Each launcher reads its
 * own switch: the test-table launcher reads {@code advancedTracerTest}, the run/exec launcher reads
 * {@code advancedTracerExec}. Only the switch of the menu that is open belongs to the launcher about to
 * run — ticking the other one closes that menu and the Trace button goes away with it.
 */
final class AdvancedTracerSupport {

    private static final Logger LOGGER = LogManager.getLogger(AdvancedTracerSupport.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));

    private AdvancedTracerSupport() {
    }

    /**
     * Asks for the advanced step debugger before the trace window is opened. Both switches may be hidden —
     * only "checked" is read by the launcher, so the click event is dispatched instead of a real click.
     *
     * @return whether a switch was reachable and is now set
     */
    static boolean requestAdvancedTracer(Page page) {
        WebElement execCheckbox = new WebElement(page, "css=input#advancedTracerExec", "advancedTracerExec");
        WebElement testCheckbox = new WebElement(page, "css=input#advancedTracerTest", "advancedTracerTest");
        for (WebElement checkbox : List.of(execCheckbox, testCheckbox)) {
            if (checkbox.isVisible(1000)) {
                if (!checkbox.isCheckedEvenIfHidden()) {
                    LOGGER.info("Asking the launcher for the advanced tracer");
                    checkbox.checkEvenIfHidden();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Last resort for a table the launcher runs without asking for parameters: its switch is in a block the
     * page keeps hidden, and a programmatic click does not flip a hidden checkbox, so the window is re-opened
     * with the flag. Costly — leaving the first document releases the debug session — so it is only used when
     * the switch could not be reached.
     */
    static void reopenInAdvancedTracer(Page tracePopup) {
        String url = tracePopup.url();
        if (url.contains("advanced=true")) {
            return;
        }
        LOGGER.info("Re-opening the trace window in the advanced debugger");
        tracePopup.navigate(url + (url.contains("?") ? "&" : "?") + "advanced=true");
        tracePopup.waitForLoadState();
        tracePopup.waitForSelector("xpath=//div[@id='trace-view']",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT_MS));
    }
}
