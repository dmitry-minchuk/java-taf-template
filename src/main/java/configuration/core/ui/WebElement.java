package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static configuration.driver.LocalDriverPool.ExecutionMode.PLAYWRIGHT_DOCKER;

public class WebElement {
    
    protected final static Logger LOGGER = LogManager.getLogger(WebElement.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));
    // React full-screen click shield raised while the app loader is busy (EPBDS-16241 / EPBDS-16261).
    // It has pointer-events:auto and covers the whole viewport, so any pointer interaction started
    // while it is up is intercepted and times out. We wait for it to detach before interacting.
    public static final String LOADING_OVERLAY_SELECTOR = "[data-testid=loading-overlay]";
    private static final int OVERLAY_IDLE_TIMEOUT_MS = resolveOverlayIdleTimeout();
    private final int timeoutInMilliseconds;
    @Getter
    private final Page page;
    @Getter
    private final String selector;
    @Getter
    private final Locator locator;
    private final String elementName;
    @Getter
    private final WebElement parentElement;
    
    public WebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public WebElement(Page page, String selector, String elementName) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public WebElement(WebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = "Element";
        this.parentElement = parent; // Store parent reference for scoping
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    public WebElement(WebElement parent, String selector, String elementName) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.parentElement = parent; // Store parent reference for scoping
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    
    // Smart wait: block until the app is idle (no full-screen loading overlay) before interacting.

    private static int resolveOverlayIdleTimeout() {
        try {
            return Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.OVERLAY_IDLE_TIMEOUT));
        } catch (RuntimeException e) {
            return 60000;
        }
    }

    /**
     * Waits until the React full-screen loading overlay is not present in the DOM, i.e. the app has
     * finished the background repository refresh / module recompile that would otherwise intercept
     * pointer events. Returns immediately when the overlay is absent, so it is cheap on every call and
     * only spends real time on genuinely heavy recompiles (large client projects). Never throws — if the
     * overlay is still up after the timeout we proceed and let Playwright's own actionability retry.
     */
    public static void waitForAppReady(Page page, long timeoutMs) {
        try {
            page.waitForFunction(
                    "() => !document.querySelector('" + LOADING_OVERLAY_SELECTOR + "')",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
        } catch (RuntimeException ignored) {
            LOGGER.debug("Loading overlay still present after {}ms; proceeding and relying on actionability retry", timeoutMs);
        }
    }

    /** Overload using the configured overlay-idle timeout — for explicit use from page objects/components. */
    public static void waitForAppReady(Page page) {
        waitForAppReady(page, OVERLAY_IDLE_TIMEOUT_MS);
    }

    /**
     * Stronger form of {@link #waitForAppReady}: waits until the loading overlay has been ABSENT
     * continuously for a short quiet window, i.e. the background refresh / recompile has truly finished
     * re-rendering. A single absent instant is not enough during a commit — the overlay flickers, so
     * {@link #waitForAppReady} can return mid-recompile while JSF nodes are still detaching. Never throws.
     */
    public static void waitForAppIdle(Page page, long timeoutMs) {
        try {
            page.evaluate("() => { window.__openlIdleSince = 0; }");
            page.waitForFunction(
                    "() => { const now = performance.now();" +
                    " if (document.querySelector('" + LOADING_OVERLAY_SELECTOR + "')) { window.__openlIdleSince = 0; return false; }" +
                    " if (!window.__openlIdleSince) { window.__openlIdleSince = now; return false; }" +
                    " return (now - window.__openlIdleSince) >= 750; }",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
        } catch (RuntimeException ignored) {
            LOGGER.debug("App still busy after {}ms; proceeding and relying on actionability retry", timeoutMs);
        }
    }

    private void waitForAppReady() {
        waitForAppReady(page, OVERLAY_IDLE_TIMEOUT_MS);
    }

    /**
     * Runs a pointer/keyboard interaction that can transiently fail while the app is doing a background
     * refresh / recompile: the target keeps re-rendering ("element ... detached from the DOM", "not
     * stable") or the loading overlay flickers back up mid-action ("intercepts pointer events").
     * Playwright already retries within a single call up to its own timeout, but on loaded CI that
     * window can be shorter than the recompile, so we additionally re-wait for the app to go idle and
     * retry the whole interaction until it lands or the overlay-idle budget is spent. Non-churn failures
     * (element genuinely absent, bad selector) are rethrown at once so real defects still fail fast, and
     * when there is no churn the action runs exactly once with zero overhead (EPBDS-16261).
     */
    private void retryOnReRenderChurn(Runnable action) {
        long deadline = System.currentTimeMillis() + OVERLAY_IDLE_TIMEOUT_MS;
        while (true) {
            try {
                action.run();
                return;
            } catch (PlaywrightException e) {
                if (System.currentTimeMillis() >= deadline || !isTransientReRenderChurn(e)) {
                    throw e;
                }
                LOGGER.debug("{}: background re-render churn, re-waiting app-ready and retrying ({})",
                        elementName, firstLine(e.getMessage()));
                waitForAppReady();
            }
        }
    }

    private static boolean isTransientReRenderChurn(PlaywrightException e) {
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        // The loading overlay flickering back up is transient; a real blocking modal (ant-modal-wrap)
        // is NOT — scope the pointer-intercept case to the overlay so genuine modal blocks fail fast.
        boolean overlayIntercept = m.contains("intercepts pointer events") && m.contains("loading-overlay");
        return m.contains("detached from the DOM")
                || m.contains("element is not stable")
                || overlayIntercept;
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "";
        }
        int nl = message.indexOf('\n');
        return (nl >= 0 ? message.substring(0, nl) : message).trim();
    }

    // Core Actions

    public void click() {
        waitForAppReady();
        isVisible();
        LOGGER.info("Clicking {} ", elementName);
        retryOnReRenderChurn(locator::click);
    }

    public void click(int timeoutInMillis) {
        waitForAppReady();
        isVisible(timeoutInMillis);
        LOGGER.info("Clicking with increased timeout {} ", elementName);
        retryOnReRenderChurn(() -> locator.click(new Locator.ClickOptions().setTimeout(timeoutInMillis)));
    }

    public void clickForce() {
        LOGGER.info("Force clicking {} (bypassing visibility checks)", elementName);
        locator.click(new Locator.ClickOptions().setForce(true));
    }

    public void clickForce(int timeoutInMillis) {
        LOGGER.info("Force clicking {} with timeout (bypassing visibility checks)", elementName);
        locator.click(new Locator.ClickOptions().setForce(true).setTimeout(timeoutInMillis));
    }

    // For JSF buttons that trigger a commit/recompile and re-render themselves during it: the JSF
    // toolbar re-renders on its own a4j cycle (not tracked by the React overlay), so no overlay wait
    // reliably catches a "stable" instant and a normal click's actionability thrashes the churn. A
    // human just clicks once and the onclick fires. So: wait for the app to settle, then dispatch the
    // click event straight to the element's onclick handler (no actionability/visibility checks).
    public void clickWhenSettled() {
        waitForAppIdle(page, OVERLAY_IDLE_TIMEOUT_MS);
        LOGGER.info("Dispatching click (after app settled) {}", elementName);
        locator.dispatchEvent("click");
    }

    public WebElement doubleClick() {
        waitForAppReady();
        isVisible();
        LOGGER.info("Double clicking {} ", elementName);
        retryOnReRenderChurn(locator::dblclick);
        return this;
    }

    public WebElement press(String key) {
        waitForAppReady();
        isVisible();
        LOGGER.info("Pressing {} on {}", key, elementName);
        retryOnReRenderChurn(() -> locator.press(key));
        return this;
    }

    public WebElement fill(String text) {
        waitForAppReady();
        isVisible();
        LOGGER.info("Filling {} with text: '{}'", elementName, text);
        retryOnReRenderChurn(() -> locator.fill(text));
        return this;
    }

    public WebElement fillSequentially(String text) {
        waitForAppReady();
        isVisible();
        LOGGER.info("Filling Sequentially {} with text: '{}'", elementName, text);
        retryOnReRenderChurn(() -> locator.pressSequentially(text));
        return this;
    }
    
    public String getText() {
        return getText(true);
    }

    public String getText(boolean log) {
        isVisible();
        String text = locator.textContent();
        if (log) {
            LOGGER.info("Getting text from {}: '{}'", elementName, text);
        }
        return text;
    }

    public String getText(int timeoutInMillis) {
        WaitUtil.sleep(timeoutInMillis, "Sleeping before getting text from " + elementName);
        return getText();
    }

    public String getInnerText() {
        isVisible();
        String text = locator.innerText();
        LOGGER.info("Getting inner text from {}: '{}'", elementName, text);
        return text;
    }

    public String getInnerText(int timeoutInMillis) {
        WaitUtil.sleep(timeoutInMillis, "Sleeping before getting inner text from " + elementName);
        return getInnerText();
    }

    public String getAttribute(String name) {
        String value = WaitUtil.retryOnException(
                () -> locator.getAttribute(name, new Locator.GetAttributeOptions().setTimeout(500)),
                DEFAULT_TIMEOUT_MS, 300,
                "Getting attribute '" + name + "' from " + elementName
        );
        LOGGER.info("Getting attribute '{}' from {}: '{}'", name, elementName, value);
        return value;
    }

    public String getCurrentInputValue() {
        String value = locator.inputValue();
        LOGGER.info("Getting current input value from {}: '{}'", elementName, value);
        return value;
    }
    
    // Visibility and State Checks
    
    public boolean isVisible() {
        try {
            waitForVisible(250);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(int timeoutInMillis) {
        try {
            waitForVisible(timeoutInMillis);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public boolean isChecked() {
        isVisible();
        return locator.isChecked();
    }

    public void check() {
        waitForAppReady();
        isVisible();
        retryOnReRenderChurn(locator::check);
    }

    public void uncheck() {
        waitForAppReady();
        isVisible();
        retryOnReRenderChurn(locator::uncheck);
    }
    
    // Selection methods for dropdowns

    // This method does not require clicking by selector - everything will be done automatically if selector implemented as <select>
    public void selectByVisibleText(String text) {
        waitForAppReady();
        isVisible();
        LOGGER.info("Selecting option '{}' in {}", text, elementName);
        List<String> optionLabels = locator.locator("option").allTextContents().stream()
                .map(String::trim)
                .toList();
        @SuppressWarnings("unchecked")
        List<String> optionValues = (List<String>) locator.locator("option")
                .evaluateAll("options => options.map(option => option.value || '')");
        if (optionLabels.contains(text)) {
            retryOnReRenderChurn(() -> locator.selectOption(new SelectOption().setLabel(text)));
        } else if (optionValues.contains(text)) {
            retryOnReRenderChurn(() -> locator.selectOption(text));
        } else {
            throw new IllegalArgumentException("Option '" + text + "' was not found in " + elementName
                    + ". Labels: " + optionLabels + ". Values: " + optionValues);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getSelectValues() {
        isVisible();
        LOGGER.info("Getting select option values from {}", elementName);
        return (List<String>) locator.locator("option").evaluateAll("options => options.map(option => option.value || '')");
    }

    public List<String> getSelectVisibleTextValues() {
        isVisible();
        LOGGER.info("Getting select option texts from {}", elementName);
        return locator.locator("option").allTextContents();
    }

    public String getSelectedOptionText() {
        LOGGER.info("Getting selected option text from {}", elementName);
        return (String) locator.evaluate("el => el.options[el.selectedIndex].text");
    }
    
    public WebElement child(String subSelector) {
        return new WebElement(this, subSelector, elementName + " > " + subSelector);
    }

    // Dynamic locator formatting - Builder pattern implementation
    public WebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        if (this.parentElement != null) {
            return new WebElement(this.parentElement, formattedSelector, this.elementName);
        }
        return new WebElement(this.page, formattedSelector, this.elementName);
    }

    // Utility methods
    
    public void clear() {
        waitForAppReady();
        LOGGER.info("Clearing {}", elementName);
        locator.isVisible();
        retryOnReRenderChurn(locator::clear);
    }

    public void clearByKeyCombination() {
        String execMode = System.getProperty("execution.mode");
        String modifierKey = System.getProperty("os.name").toLowerCase().contains("mac") ? "Meta" : "Control";

        LOGGER.info("'Control+a' + 'Backspace' + 'Enter' {}", elementName);
        locator.isVisible();
        sleep(50);
        locator.click();
        sleep(50);
        if(execMode != null && execMode.equalsIgnoreCase(String.valueOf(PLAYWRIGHT_DOCKER)))
            locator.press("Control+a");
        else
            locator.press(modifierKey + "+a");
        sleep(50);
        locator.press("Backspace");
        sleep(50);
        locator.press("Tab");
        sleep(50);
        locator.click();
        sleep(50);
    }
    
    public void sendKeys(CharSequence... keysToSend) {
        waitForAppReady();
        String text = String.join("", keysToSend);
        LOGGER.info("Sending keys '{}' to {}", text, elementName);
        
        // Check if this is a file input element - if so, use setInputFiles instead
        String inputType = locator.getAttribute("type");
        if ("file".equals(inputType)) {
            LOGGER.info("Detected file input, using setInputFiles instead of sendKeys");
            setInputFiles(text);
            return;
        }
        
        clear();
        retryOnReRenderChurn(() -> locator.fill(text));
    }
    
    // File upload method for input[type=file] elements  
    public void setInputFiles(String... filePaths) {
        LOGGER.info("Setting input files {} to {}", Arrays.toString(filePaths), elementName);
        Path[] paths = Arrays.stream(filePaths)
            .map(Paths::get)
            .toArray(Path[]::new);
        locator.setInputFiles(paths);
    }
    
    // Explicit wait methods
    
    public WebElement waitForVisible() {
        LOGGER.info("Waiting for {} to be visible", elementName);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return this;
    }
    
    public WebElement waitForVisible(long timeoutInMillis) {
        LOGGER.info("Waiting for {} to be visible (timeout: {}s)", elementName, timeoutInMillis);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutInMillis));
        return this;
    }
    
    public WebElement waitForHidden(long timeoutInMillis) {
        LOGGER.info("Waiting for {} to be hidden", elementName);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(timeoutInMillis));
        return this;
    }

    public WebElement sleep(long timeoutInMillis) {
        LOGGER.info("Sleeping for {} ", elementName);
        WaitUtil.sleep(timeoutInMillis, "Element sleep for " + elementName);
        return this;
    }
    
    // Interaction methods
    
    public WebElement hover() {
        waitForAppReady();
        LOGGER.info("Hovering over {}", elementName);
        retryOnReRenderChurn(locator::hover);
        return this;
    }
    
    // CSS properties
    
    public String getCssValue(String propertyName) {
        LOGGER.info("Getting CSS property '{}' from {}", propertyName, elementName);
        return locator.evaluate("el => window.getComputedStyle(el).getPropertyValue('" + propertyName + "')").toString();
    }

    public boolean isEnabled() {
        LOGGER.info("Checking if {} is enabled", elementName);
        return locator.isEnabled();
    }
}
