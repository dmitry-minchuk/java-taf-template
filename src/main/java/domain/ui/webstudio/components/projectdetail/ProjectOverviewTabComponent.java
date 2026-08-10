package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * The Overview tab of the React project-detail view: the right info column (Status / Repository / Path /
 * Branch / Revision / Modified / Comment), the branch switcher, tags and the module list. Scoped under
 * {@code [data-testid=overview-panel]} (verified against the live 6.4.0 DOM); the branch switcher menu is
 * an antd dropdown rendered into a body-level portal, so its items are located at page level on purpose.
 */
public class ProjectOverviewTabComponent extends BaseComponent {

    // The default branch is tagged in the label, and the text comes back glued together ("masterDefault").
    private static final String DEFAULT_BRANCH_TAG = "Default";
    private static final int PROBE_MS = 1500;

    private final WebElement branchLabel;
    private final WebElement branchSwitcherTrigger;
    // antd dropdown portal — rendered at body level, outside the overview subtree
    private final WebElement branchMenuItem;
    private final WebElement modifiedValue;
    private final WebElement modifiedDate;
    private final WebElement tagValueForType;
    private final WebElement overviewRight;
    // Overview lists the project's modules as <li data-testid="module-<path or name>"> rows; the same prefix
    // is also used for a module's filter and matched-wildcard rows, which are not modules of their own.
    private final WebElement moduleRows;
    private final WebElement editBtn;
    private final WebElement saveBtn;

    public ProjectOverviewTabComponent(Page page) {
        this(new WebElement(page, "[data-testid=overview-panel]", "overviewPanel"));
    }

    public ProjectOverviewTabComponent(WebElement rootLocator) {
        super(rootLocator);
        branchLabel = createScopedElement("[data-testid=overview-branch]", "branchLabel");
        branchSwitcherTrigger = createScopedElement("[data-testid=overview-branch-trigger]", "branchSwitcherTrigger");
        // A switcher entry shows the branch name plus its marks ("master" + a Default tag), so match it by
        // the menu key antd derives from the branch name, falling back to the name held inside the label.
        branchMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][@data-menu-id='rc-menu-uuid-%s' or .//*[normalize-space()='%s']]", "branchMenuItem");
        // In the Overview panel a field is a label <div><span>Name</span></div> followed by its value div;
        // the Modified value holds the author and, in a nested div, the date.
        modifiedValue = createScopedElement("xpath=.//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]", "modifiedValue");
        modifiedDate = createScopedElement("xpath=.//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]/div[last()]", "modifiedDate");
        // Tags render in their own panel as pairs of spans: the type, then its value.
        tagValueForType = createScopedElement("xpath=.//*[@data-testid='project-tags']/span[normalize-space()='%s']/following-sibling::span[1]", "tagValueForType");
        overviewRight = createScopedElement("[data-testid=overview-right]", "overviewRight");
        moduleRows = createScopedElement(
                "xpath=.//li[starts-with(@data-testid,'module-')]"
                        + "[not(starts-with(@data-testid,'module-filter-'))]"
                        + "[not(starts-with(@data-testid,'module-matched-'))]"
                        + "[not(starts-with(@data-testid,'module-unmatched-'))]",
                "overviewModuleRows");
        editBtn = createScopedElement("[data-testid=overview-edit]", "overviewEditBtn");
        saveBtn = createScopedElement("[data-testid=overview-save]", "overviewSaveBtn");
    }

    /** Name of the branch the project sits on, without the Default tag the label adds to it. */
    public String getCurrentBranch() {
        String label = branchLabel.getText().trim();
        if (label.endsWith(DEFAULT_BRANCH_TAG)) {
            label = label.substring(0, label.length() - DEFAULT_BRANCH_TAG.length());
        }
        return label.trim();
    }

    // Switches the project onto another branch via the branch switcher next to the branch label.
    public void switchBranch(String branchName) {
        branchSwitcherTrigger.click();
        branchMenuItem.format(branchName, branchName).click();
        waitUntilSpinnerLoaded();
    }

    /**
     * Whether the repository holds this branch. The switcher offers every branch except the one the project
     * is already on, so the current branch is answered directly.
     */
    public boolean isBranchPresent(String branchName) {
        if (branchName.equals(getCurrentBranch())) {
            return true;
        }
        branchSwitcherTrigger.click();
        boolean present = branchMenuItem.format(branchName, branchName).isVisible(PROBE_MS);
        page.keyboard().press("Escape");
        return present;
    }

    // Reads the value assigned for a tag type from the Overview TAGS section (each tag renders as an
    // ant-tag "<type> → <value>"); returns the value span's text.
    public String getTagValueForType(String tagType) {
        return tagValueForType.format(tagType).getText().trim();
    }

    // The Overview-right column concatenates its labelled fields into one text blob, in the order
    // Status / Repository / Path / Branch / Revision ID / Modified / Comment. Extracts one field's value.
    public String extractField(String label, String nextLabel) {
        String blob = overviewRight.getText();
        int start = blob.indexOf(label);
        if (start < 0) {
            return "";
        }
        start += label.length();
        int end = blob.indexOf(nextLabel, start);
        return (end < 0 ? blob.substring(start) : blob.substring(start, end)).trim();
    }

    /** Names of the modules the Overview tab lists for the project. */
    public List<String> getModuleNames() {
        editBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
        Locator rows = moduleRows.getLocator();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < rows.count(); i++) {
            names.add(rows.nth(i).innerText().trim().split("\n")[0].trim());
        }
        return names;
    }

    /** Switches the Overview tab into edit mode and saves it without changing anything. */
    public void editAndSave() {
        editBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        saveBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        waitUntilSpinnerLoaded();
    }

    /** Who last changed the project — the author part of the Overview "Modified" field. */
    public String getModifiedBy() {
        String whole = modifiedValue.getText().trim();
        String date = modifiedDate.getText().trim();
        return whole.endsWith(date) ? whole.substring(0, whole.length() - date.length()).trim() : whole;
    }

    /** When the project was last changed, as the Overview shows it (e.g. "Jul 27, 2026 10:26 AM"). */
    public String getModifiedAt() {
        return modifiedDate.getText().trim();
    }
}
