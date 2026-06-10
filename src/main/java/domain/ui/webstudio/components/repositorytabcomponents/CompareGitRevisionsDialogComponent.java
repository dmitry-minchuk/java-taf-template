package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
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
        super(LocalDriverPool.getPage());
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
        leftModulesSelect = new WebElement(getPage(), "xpath=(//select[contains(@name,'compareForm')])[1]", "leftModulesSelect");
        rightModulesSelect = new WebElement(getPage(), "xpath=(//select[contains(@name,'compareForm')])[4]", "rightModulesSelect");
        closeBtn = new WebElement(getPage(), "xpath=//input[@value='Close']", "closeBtn");
        revisionSelect = new WebElement(getPage(),
                "xpath=//select[@name='compareForm:repositoryRevision']",
                "revisionSelect");
        compareBtnInPopup = new WebElement(getPage(),
                "xpath=//input[@id='compareForm:compareBtn']",
                "compareBtnInPopup");
        showEqualRowsCheckbox = new WebElement(getPage(),
                "xpath=//input[contains(@id,'compareForm:compareBtn')]/preceding-sibling::input[@type='checkbox'][1]",
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
        // idSuffix pattern: "1_te_c-7:5" - uses ends-with to avoid substring collisions (e.g. c-2:1 vs c-2:10)
        cellTemplate = new WebElement(getPage(),
                "xpath=//td[(contains(@id,'diffTreeForm') or contains(@id,'compareRevisionsForm')) and (substring(@id, string-length(@id) - string-length('%1$s') + 1) = '%1$s')]",
                "cellTemplate");
        editorRowsTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:editor%1$s_te_table' or @id='compareRevisionsForm:editor%1$s_te_table']//tr[./td]",
                "editorRowsTemplate");
    }

    public CompareGitRevisionsDialogComponent waitForDialogToAppear() {
        WaitUtil.sleep(1500, "Waiting for Compare dialog to appear");
        leftModulesSelect.waitForVisible(5000);
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
        String idSuffix = fragment + "_te_c-" + row + ":" + col;
        WebElement cell = cellTemplate.format(idSuffix);
        cell.waitForVisible(5000);
        String actualColor = cell.getCssValue("background-color");
        LOGGER.info("Repo cell background-color at [{},{}] fragment={}: {}", row, col, fragment, actualColor);
        return actualColor.equals(colorRGBA);
    }

    // ========== Row counting (repository-specific locator) ==========

    public int getNumberOfRows(int fragment) {
        WaitUtil.waitForCondition(
                () -> editorRowsTemplate.format(String.valueOf(fragment)).getLocator().count() > 0,
                10000,
                250,
                "Waiting for repository comparison rows to appear for fragment " + fragment);
        return editorRowsTemplate.format(String.valueOf(fragment)).getLocator().count();
    }

    // ========== Show Equal Rows ==========

    public void setShowEqualRows(boolean value) {
        if (showEqualRowsCheckbox.isChecked() != value) {
            showEqualRowsCheckbox.click();
            WaitUtil.sleep(300, "Waiting for showEqualRows toggle in repo compare");
        }
    }

    public void close() {
        if (comparePopup != null && !comparePopup.isClosed()) {
            comparePopup.close();
        }
    }
}
