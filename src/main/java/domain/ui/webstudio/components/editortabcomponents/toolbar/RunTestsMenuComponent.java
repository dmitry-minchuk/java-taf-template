package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

/**
 * The Test area of the editor's top toolbar: the Test button, its dropdown with run settings
 * (tests per page, failures only, compound result, within-current-module-only) and the launch buttons.
 * Everything is scoped under the {@code testPanel} container (verified against the live 6.4.0 DOM,
 * where {@code ul#testSettings} renders inside {@code div#testPanel}).
 */
public class RunTestsMenuComponent extends BaseComponent implements IRunTestsMenu {

    private final WebElement testBtn;
    private final WebElement dropdownToggle;
    private final WebElement perPageDropdown;
    private final WebElement failuresOnlyCheckbox;
    private final WebElement compoundResultCheckbox;
    private final WebElement runTestsBtn;
    private final WebElement withinCurrentModuleOnly;

    public RunTestsMenuComponent(Page page) {
        this(new WebElement(page, "xpath=//div[@id='testPanel']", "testPanel"));
    }

    public RunTestsMenuComponent(WebElement rootLocator) {
        super(rootLocator);
        testBtn = createScopedElement("xpath=.//a[@title='Run Tests']", "topPanelTestBtn");
        dropdownToggle = createScopedElement("xpath=.//a[@title='Run Tests']/following-sibling::span[1]", "testDropdownBtn");
        perPageDropdown = createScopedElement("xpath=.//ul[@id='testSettings']//select[@name='pp']", "testPerPageDropdown");
        failuresOnlyCheckbox = createScopedElement("xpath=.//ul[@id='testSettings']//input[@name='failuresOnly']", "failuresOnlyCheckbox");
        compoundResultCheckbox = createScopedElement("xpath=.//ul[@id='testSettings']//input[@name='complexResult']", "compoundResultCheckbox");
        runTestsBtn = createScopedElement("xpath=.//ul[@id='testSettings']//a[contains(@class,'button') and text()='Test']", "runTestsBtn");
        withinCurrentModuleOnly = createScopedElement("xpath=.//input[@id='testModuleOnlyField']", "topPanelWithinCurrentModuleOnly");
    }

    // ========== Test button ==========

    public String getTestButtonText() {
        if (testBtn.isVisible(2000)) {
            return testBtn.getText().trim();
        }
        return "";
    }

    public boolean isTestButtonVisible() {
        return testBtn.isVisible(2000);
    }

    public void clickTestButton() {
        testBtn.click();
    }

    public void runAllTests() {
        testBtn.waitForVisible();
        testBtn.click();
    }

    public void openDropdown() {
        dropdownToggle.click();
    }

    /** Opens the dropdown and waits for the module-only checkbox state to settle after the server round-trip. */
    public void openDropdownAndWaitForSettings() {
        waitUntilSpinnerLoaded();
        dropdownToggle.click();
        waitForWithinCurrentModuleOnlyToStabilize();
    }

    public void clickRunTestsButton() {
        runTestsBtn.waitForVisible();
        runTestsBtn.click();
    }

    // ========== IRunTestsMenu ==========

    @Override
    public IRunTestsMenu setTestPerPage(String testsPerPage) {
        if (testsPerPage != null && !testsPerPage.isEmpty() && !testsPerPage.equals("empty")) {
            perPageDropdown.selectByVisibleText(testsPerPage);
        }
        return this;
    }

    @Override
    public IRunTestsMenu setFailuresOnly(boolean failuresOnly) {
        if (failuresOnly != failuresOnlyCheckbox.isChecked()) {
            failuresOnlyCheckbox.click();
        }
        return this;
    }

    @Override
    public IRunTestsMenu setCompoundResult(boolean compoundResult) {
        if (compoundResult != compoundResultCheckbox.isChecked()) {
            compoundResultCheckbox.click();
        }
        return this;
    }

    @Override
    public void runTests() {
        runTestsBtn.click();
    }

    @Override
    public String getTestPerPage() {
        return perPageDropdown.getLocator().inputValue();
    }

    @Override
    public boolean isFailuresOnlyChecked() {
        return failuresOnlyCheckbox.isChecked();
    }

    @Override
    public boolean isCompoundResultChecked() {
        return compoundResultCheckbox.isChecked();
    }

    // ========== Within Current Module Only (top panel) ==========

    public boolean isWithinCurrentModuleOnlyChecked() {
        return withinCurrentModuleOnly.isChecked();
    }

    public boolean isWithinCurrentModuleOnlyEnabled() {
        return withinCurrentModuleOnly.isEnabled();
    }

    public void setWithinCurrentModuleOnly(boolean value) {
        WaitUtil.retryAction(() -> {
            withinCurrentModuleOnly.waitForVisible();
            if (!withinCurrentModuleOnly.isEnabled()) {
                throw new RuntimeException("Top panel WithinCurrentModuleOnly is disabled");
            }
            if (value) {
                withinCurrentModuleOnly.check();
            } else {
                withinCurrentModuleOnly.uncheck();
            }
            boolean settled = WaitUtil.waitForCondition(
                    () -> withinCurrentModuleOnly.isChecked() == value
                            && withinCurrentModuleOnly.isEnabled(),
                    1200, 200, "Waiting for WithinCurrentModuleOnly to settle at " + value);
            if (!settled) {
                throw new RuntimeException("WithinCurrentModuleOnly did not settle at " + value);
            }
        }, 10000, 250, "Setting top panel WithinCurrentModuleOnly to " + value);
    }

    // Waits until the checkbox's (checked, enabled) state stays unchanged for a short quiet window, i.e.
    // the server round-trip that can flip/disable it during a recompile has settled.
    private void waitForWithinCurrentModuleOnlyToStabilize() {
        long stableWindowMs = 750;
        String[] lastState = {null};
        long[] stableSince = {0};
        WaitUtil.waitForCondition(() -> {
            if (withinCurrentModuleOnly.getLocator().count() == 0) {
                lastState[0] = null;
                return false;
            }
            String state = withinCurrentModuleOnly.isChecked() + ":" + withinCurrentModuleOnly.isEnabled();
            long now = System.currentTimeMillis();
            if (!state.equals(lastState[0])) {
                lastState[0] = state;
                stableSince[0] = now;
                return false;
            }
            return now - stableSince[0] >= stableWindowMs;
        }, 10000, 150, "Waiting for WithinCurrentModuleOnly checkbox state to stabilize");
    }
}
