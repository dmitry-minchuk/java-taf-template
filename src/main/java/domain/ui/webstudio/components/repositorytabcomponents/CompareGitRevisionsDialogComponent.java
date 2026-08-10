package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CompareGitRevisionsDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CompareGitRevisionsDialogComponent.class);

    private WebElement leftModulesSelect;
    private WebElement rightModulesSelect;
    private WebElement closeBtn;
    private WebElement revisionSelect;
    private WebElement compareBtnInPopup;
    private WebElement showEqualRowsCheckbox;
    private WebElement treeNodeLabels;
    // Template: %s = nodeName
    private WebElement treeNodeExpanderTemplate;
    // Template: %s = nodeName
    private WebElement treeNodeClosedExpanderTemplate;
    // Template: %s = nodeName
    private WebElement treeNodeLinkTemplate;
    // Template: %s = idSuffix e.g. "1_te_c-7:5"
    private WebElement cellTemplate;
    // Template: %s = fragment number
    private WebElement editorRowsTemplate;
    private Page comparePopup;
    private String lastOpenedTreeNodeName;

    public CompareGitRevisionsDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public CompareGitRevisionsDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    public CompareGitRevisionsDialogComponent(Page comparePopup) {
        super(comparePopup);
        comparePopup.setDefaultTimeout(DEFAULT_TIMEOUT_MS);
        this.comparePopup = comparePopup;
        initializeElements();
    }

    private void initializeElements() {
        // The screen has four selects: the workspace module list, the branch, the revision and the
        // revision's module list. Address the two module lists directly rather than by position.
        leftModulesSelect = new WebElement(getPage(), "xpath=(//select[contains(@name,'compareForm')])[1]", "leftModulesSelect");
        rightModulesSelect = new WebElement(getPage(), "xpath=//select[@name='compareForm:repositoryExcelCombo']", "rightModulesSelect");
        closeBtn = new WebElement(getPage(), "xpath=//input[@value='Close']", "closeBtn");
        revisionSelect = new WebElement(getPage(),
                "xpath=//select[@name='compareForm:repositoryRevision']",
                "revisionSelect");
        compareBtnInPopup = new WebElement(getPage(),
                "xpath=//input[@id='compareForm:compareBtn']",
                "compareBtnInPopup");
        // Both checkboxes are rendered without an id and with a generated name (compareForm:j_idtNN), so the
        // only stable handle is their own label text.
        showEqualRowsCheckbox = new WebElement(getPage(),
                "xpath=//text()[contains(.,'Show equal rows')]/following::input[@type='checkbox'][1]",
                "showEqualRowsCheckbox");
        treeNodeLabels = new WebElement(getPage(),
                "xpath=//span[contains(@class,'rf-trn-lbl')]",
                "treeNodeLabels");
        treeNodeExpanderTemplate = new WebElement(getPage(),
                "xpath=//div[contains(@class,'rf-trn') and .//span[contains(@class,'rf-trn-lbl') and normalize-space(.)='%s']]/span[1]",
                "treeNodeExpanderTemplate");
        treeNodeClosedExpanderTemplate = new WebElement(getPage(),
                "xpath=//div[contains(@class,'rf-trn') and .//span[contains(@class,'rf-trn-lbl') and normalize-space(.)='%s']]" +
                        "/span[contains(@class,'rf-trn-hnd-colps')]",
                "treeNodeClosedExpanderTemplate");
        treeNodeLinkTemplate = new WebElement(getPage(),
                "xpath=//span[contains(@class,'rf-trn-lbl') and normalize-space(.)='%s']",
                "treeNodeLinkTemplate");
        // React repo-compare renders the diff in a standalone showDiff.xhtml tab whose JSF form id is dynamic
        // (e.g. j_idt11), so the fragment is addressed by the Nth "_te_table" div and cells by their id suffix
        // "_te_c-<row>:<col>" (%1$s = fragment 1/2, %2$s = the cell-id suffix).
        cellTemplate = new WebElement(getPage(),
                "xpath=(//div[substring(@id, string-length(@id) - 8) = '_te_table'])[%1$s]//td[substring(@id, string-length(@id) - string-length('%2$s') + 1) = '%2$s']",
                "cellTemplate");
        editorRowsTemplate = new WebElement(getPage(),
                "xpath=(//div[substring(@id, string-length(@id) - 8) = '_te_table'])[%1$s]//tr[./td]",
                "editorRowsTemplate");
    }

    /** Waits until the screen has loaded its lists (the module dropdowns fill in a moment after opening). */
    public CompareGitRevisionsDialogComponent waitForDialogToAppear() {
        compareBtnInPopup.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> !leftModulesSelect.getSelectVisibleTextValues().isEmpty(),
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the compare screen's module list");
        return this;
    }

    public List<String> getLeftModulesList() {
        return leftModulesSelect.getSelectVisibleTextValues();
    }

    public List<String> getRightModulesList() {
        return rightModulesSelect.getSelectVisibleTextValues();
    }

    // ========== Revision and Compare ==========

    public void selectRevision(int index) {
        boolean populated = WaitUtil.waitForCondition(
                () -> revisionSelect.getSelectVisibleTextValues().size() > index,
                DEFAULT_TIMEOUT_MS,
                200,
                "Waiting for revision dropdown to have at least " + (index + 1) + " options"
        );
        if (!populated) {
            throw new IllegalStateException(
                    "Revision dropdown never populated enough to pick index " + index
                            + "; available: " + revisionSelect.getSelectVisibleTextValues());
        }
        revisionSelect.getLocator().selectOption(new com.microsoft.playwright.options.SelectOption().setIndex(index));
    }

    public void clickCompareBtn() {
        compareBtnInPopup.waitForVisible(5000);
        // Changing the revision reloads the revision's Excel list over Ajax, and Compare stays disabled
        // until both file lists are filled. Clicking earlier is a no-op and leaves the tree empty.
        WaitUtil.waitForCondition(() -> !rightModulesSelect.getSelectVisibleTextValues().isEmpty()
                        && compareBtnInPopup.isEnabled(),
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the compare screen to enable the Compare button");
        compareBtnInPopup.click();
        WaitUtil.sleep(1000, "Waiting for repository comparison Ajax request to start");
        waitUntilSpinnerLoaded();
        try {
            getPage().waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(5000));
        } catch (RuntimeException e) {
            LOGGER.debug("No network idle state after repository compare click: {}", e.getMessage());
        }
        waitForComparisonTreeToLoad();
    }

    // ========== Tree navigation ==========

    public void openTreeNode(String nodeName) {
        waitForComparisonTreeToLoad();
        treeNodeExpanderTemplate.format(nodeName).waitForVisible(5000);
        if (treeNodeClosedExpanderTemplate.format(nodeName).isVisible(500)) {
            treeNodeClosedExpanderTemplate.format(nodeName).click();
        }
        lastOpenedTreeNodeName = nodeName;
        WaitUtil.sleep(250, "Waiting for repo tree node to expand: " + nodeName);
    }

    public void clickTreeNode(String nodeName) {
        WaitUtil.retryOnException(() -> {
            if (!treeNodeLinkTemplate.format(nodeName).isVisible(500) && lastOpenedTreeNodeName != null) {
                openTreeNode(lastOpenedTreeNodeName);
            }
            treeNodeLinkTemplate.format(nodeName).waitForVisible(5000);
            treeNodeLinkTemplate.format(nodeName).click();
            return true;
        }, 15000, 250, "Clicking repo tree node: " + nodeName);
        WaitUtil.sleep(500, "Waiting after clicking repo tree node: " + nodeName);
    }

    private void waitForComparisonTreeToLoad() {
        boolean loaded = WaitUtil.waitForCondition(
                () -> treeNodeLabels.getLocator().count() > 0,
                15000,
                250,
                "Waiting for repository compare tree items to load");
        if (!loaded) {
            throw new IllegalStateException("Repository compare tree items did not load");
        }
    }

    // ========== Cell highlighting ==========

    public boolean isCellHighlightedGreen(int row, int col, String fragment) {
        return isCellHighlightedWithColor(row, col, fragment, "rgb(195, 214, 155)");
    }

    public boolean isCellHighlightedWithColor(int row, int col, String fragment, String colorRGBA) {
        String idSuffix = "_te_c-" + row + ":" + col;
        WebElement cell = cellTemplate.format(fragment, idSuffix);
        cell.waitForVisible(5000);
        String actualColor = cell.getCssValue("background-color");
        LOGGER.info("Repo cell background-color at [{},{}] fragment={}: {}", row, col, fragment, actualColor);
        return actualColor.equals(colorRGBA);
    }

    // ========== Row counting (repository-specific locator) ==========

    // showDiff.xhtml renders the whole table in the DOM and hides equal rows via CSS when "Show equal
    // elements" is off, so count only the VISIBLE rows (the old build removed equal rows from the DOM).
    public int getNumberOfRows(int fragment) {
        Locator rows = editorRowsTemplate.format(String.valueOf(fragment)).getLocator();
        WaitUtil.waitForCondition(() -> rows.count() > 0, 10000, 250,
                "Waiting for repository comparison rows to appear for fragment " + fragment);
        int total = rows.count();
        int visible = 0;
        for (int i = 0; i < total; i++) {
            if (rows.nth(i).isVisible()) {
                visible++;
            }
        }
        return visible;
    }

    // ========== Show Equal Rows ==========

    // Toggling "Show equal elements" re-renders the whole diff (the tree collapses), so callers must
    // re-navigate the tree afterwards; here we just flip it and wait for the tree to rebuild.
    // Flips the "Show equal elements" checkbox and waits for the diff to re-render. NOTE: on this build's
    // showDiff.xhtml the toggle no longer removes equal rows from the fragment tables (the diff always renders
    // the full table with changed cells highlighted), so it is a UI-state toggle rather than a row filter.
    public void setShowEqualRows(boolean value) {
        if (showEqualRowsCheckbox.isChecked() != value) {
            showEqualRowsCheckbox.click();
            WaitUtil.sleep(500, "Waiting for diff re-render after show-equal toggle in repo compare");
            waitForComparisonTreeToLoad();
        }
    }

    public void close() {
        if (comparePopup != null && !comparePopup.isClosed()) {
            comparePopup.close();
        }
    }
}
