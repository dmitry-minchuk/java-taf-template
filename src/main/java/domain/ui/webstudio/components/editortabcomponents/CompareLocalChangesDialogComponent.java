package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class CompareLocalChangesDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CompareLocalChangesDialogComponent.class);

    private WebElement treeContainer;
    private WebElement closeBtn;
    private List<WebElement> treeItems;
    private WebElement firstFragment;
    private WebElement secondFragment;
    private WebElement showEqualRowsCheckbox;

    // Template: %s = nodeName
    private WebElement treeNodeExpanderTemplate;
    // Template: %s = nodeName
    private WebElement treeNodeLinkTemplate;
    // Template: %s = name
    private WebElement treeItemTemplate;
    // Template: %s = idSuffix e.g. "1_te_c-7:5" (fragment_te_c-row:col)
    // Works for diffTreeForm and compareRevisionsForm
    private WebElement cellTemplate;
    // Template: %s = idSuffix — only diffTreeForm (used for neighbour cell in isCellHighlighted)
    private WebElement neighbourCellTemplate;
    // Template: %s = idSuffix — editor1/editor2 cell id
    private WebElement editorCellTemplate;
    // Template: %s = idSuffix — tableEditor1/tableEditor2 cell id
    private WebElement tableEditorCellTemplate;
    // Template: %s = fragment number — tableEditor rows (local changes compare)
    private WebElement tableEditorRowsTemplate;
    // Template: %s = fragment number — comparison-layout rows
    private WebElement comparisonLayoutRowsTemplate;
    // Text diff sections (for non-Excel conflict compare)
    private List<WebElement> textDiffSections;

    private Page comparePopup;

    public CompareLocalChangesDialogComponent(Page comparePopup) {
        super(comparePopup);
        this.comparePopup = comparePopup;
        initializeElements();
    }

    // Use when the comparison opens inline (nested modal) rather than as a browser popup.
    // close() becomes a no-op so the caller controls dialog lifecycle.
    public CompareLocalChangesDialogComponent(Page page, boolean inlineModal) {
        super(page);
        this.comparePopup = null;
        initializeElements();
    }

    private void initializeElements() {
        treeContainer = new WebElement(getPage(), "xpath=//div[@id='diffTreeForm:newTree']", "treeContainer");
        closeBtn = new WebElement(getPage(), "xpath=//input[@value='Close']", "closeBtn");
        treeItems = createElementList("xpath=//div[@id='diffTreeForm:newTree']//span[@class='rf-trn-lbl']", "treeItems");
        firstFragment = new WebElement(getPage(),
                "xpath=//div[contains(@id,'diffTreeForm') and contains(@id,'1')]//table//tbody",
                "firstFragment");
        secondFragment = new WebElement(getPage(),
                "xpath=//div[contains(@id,'diffTreeForm') and contains(@id,'2')]//table//tbody",
                "secondFragment");
        showEqualRowsCheckbox = new WebElement(getPage(),
                "xpath=//form[contains(.,'equal rows')]//input[contains(@id,'idt') or contains(@name,'showEqualRows')]",
                "showEqualRowsCheckbox");
        treeNodeExpanderTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:newTree' or contains(@id,'compareRevisionsForm:newTree')]" +
                        "//div[contains(@class,'rf-trn') and .//span[contains(@class,'rf-trn-lbl') and text()='%s']]" +
                        "/span[contains(@class,'colps') or contains(@class,'exp')]",
                "treeNodeExpanderTemplate");
        treeNodeLinkTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:newTree' or contains(@id,'compareRevisionsForm:newTree')]" +
                        "//div[contains(@class,'rf-trn') and .//span[contains(@class,'rf-trn-lbl') and text()='%s']]/span[2]/span",
                "treeNodeLinkTemplate");
        treeItemTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:newTree']//span[@class='rf-trn-lbl' and text()='%s']",
                "treeItemTemplate");
        // idSuffix pattern: "1_te_c-7:5" — uses ends-with to avoid substring collisions (e.g. c-2:1 vs c-2:10)
        cellTemplate = new WebElement(getPage(),
                "xpath=//td[(contains(@id,'diffTreeForm') or contains(@id,'compareRevisionsForm')) and (substring(@id, string-length(@id) - string-length('%1$s') + 1) = '%1$s')]",
                "cellTemplate");
        neighbourCellTemplate = new WebElement(getPage(),
                "xpath=//td[contains(@id,'diffTreeForm') and (substring(@id, string-length(@id) - string-length('%1$s') + 1) = '%1$s')]",
                "neighbourCellTemplate");
        editorCellTemplate = new WebElement(getPage(),
                "xpath=//td[@id='diffTreeForm:editor%s']",
                "editorCellTemplate");
        tableEditorCellTemplate = new WebElement(getPage(),
                "xpath=//td[@id='diffTreeForm:tableEditor%s']",
                "tableEditorCellTemplate");
        tableEditorRowsTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:tableEditor%s_te_table']//tr[./td]",
                "tableEditorRowsTemplate");
        comparisonLayoutRowsTemplate = new WebElement(getPage(),
                "xpath=//table[@class='comparison-layout']/tbody/tr[2]/td[%s]//tr[./td]",
                "comparisonLayoutRowsTemplate");
        textDiffSections = createElementList(
                "xpath=//div[@class='d2h-files-diff']/div",
                "textDiffSections");
    }

    public CompareLocalChangesDialogComponent waitForDialogToAppear() {
        WaitUtil.sleep(1500, "Waiting for Local Changes Compare dialog to appear");
        treeContainer.waitForVisible(5000);
        return this;
    }

    public CompareLocalChangesDialogComponent waitForTextCompareToAppear() {
        WaitUtil.waitForCondition(
                () -> textDiffSections.stream().anyMatch(s -> s.isVisible(500)),
                10000, 250, "Waiting for text diff sections to appear");
        return this;
    }

    public List<String> getLeftModulesList() {
        return treeItems.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    // ========== Tree navigation ==========

    public void openTreeNode(String nodeName) {
        treeNodeExpanderTemplate.format(nodeName).click();
        WaitUtil.sleep(500, "Waiting for tree node to expand: " + nodeName);
    }

    public void clickTreeNode(String nodeName) {
        treeNodeLinkTemplate.format(nodeName).click();
        WaitUtil.sleep(500, "Waiting after clicking tree node: " + nodeName);
    }

    public boolean isTreeItemPresent(String name) {
        return treeItemTemplate.format(name).isVisible(1000);
    }

    // ========== Fragment presence ==========

    public boolean isFirstFragmentPresent() {
        return firstFragment.isVisible(2000);
    }

    public boolean isSecondFragmentPresent() {
        return secondFragment.isVisible(2000);
    }

    // ========== Cell content ==========

    public String getCellContent(int fragment, int row, int col) {
        String idSuffix = fragment + "_te_c-" + row + ":" + col;
        WebElement editorCell = editorCellTemplate.format(idSuffix);
        WebElement tableEditorCell = tableEditorCellTemplate.format(idSuffix);
        if (editorCell.isVisible(500)) {
            return editorCell.getText().trim();
        }
        return tableEditorCell.getText().trim();
    }

    // ========== Cell highlighting ==========

    public boolean isCellHighlightedGreen(int row, int col, String fragment) {
        return isCellHighlightedWithColor(row, col, fragment, "rgb(195, 214, 155)");
    }

    public boolean isCellHighlightedWhite(int row, int col, String fragment) {
        return isCellHighlightedWithColor(row, col, fragment, "rgb(255, 255, 255)");
    }

    public boolean isCellHighlightedWithColor(int row, int col, String fragment, String colorRGBA) {
        String idSuffix = fragment + "_te_c-" + row + ":" + col;
        WebElement cell = cellTemplate.format(idSuffix);
        cell.waitForVisible(5000);
        String actualColor = cell.getCssValue("background-color");
        LOGGER.info("Cell background-color at [{},{}] fragment={}: {}", row, col, fragment, actualColor);
        return actualColor.equals(colorRGBA);
    }

    public boolean isCellHighlighted(int row, int col, int fragment) {
        String cellSuffix = fragment + "_te_c-" + row + ":" + col;
        WebElement cell = cellTemplate.format(cellSuffix);
        cell.waitForVisible(5000);
        String color = cell.getCssValue("background-color");
        LOGGER.info("Cell [{},{}] fragment={} background-color: {}", row, col, fragment, color);
        return !color.equals("rgb(255, 255, 255)") && !color.equals("rgba(0, 0, 0, 0)");
    }

    public boolean isCellContainsExpectedValue(int row, int col, String fragment, String expectedValue) {
        String idSuffix = fragment + "_te_c-" + row + ":" + col;
        String value = cellTemplate.format(idSuffix).getText().trim();
        LOGGER.info("Cell value at [{},{}] fragment={}: '{}'", row, col, fragment, value);
        return value.equalsIgnoreCase(expectedValue);
    }

    // ========== Row and column counting ==========

    public int getNumberOfRows(int fragment) {
        String frag = String.valueOf(fragment);
        WaitUtil.waitForCondition(() -> {
            int c = tableEditorRowsTemplate.format(frag).getLocator().count();
            if (c == 0) c = comparisonLayoutRowsTemplate.format(frag).getLocator().count();
            return c > 0;
        }, 10000, 250, "Waiting for comparison rows to appear for fragment " + fragment);
        int count = tableEditorRowsTemplate.format(frag).getLocator().count();
        if (count == 0) {
            count = comparisonLayoutRowsTemplate.format(frag).getLocator().count();
        }
        return count;
    }

    public int getNumberOfColumns(int fragment) {
        String frag = String.valueOf(fragment);
        com.microsoft.playwright.Locator rows = tableEditorRowsTemplate.format(frag).getLocator();
        if (rows.count() == 0) {
            rows = comparisonLayoutRowsTemplate.format(frag).getLocator();
        }
        int maxCols = 0;
        for (int i = 0; i < rows.count(); i++) {
            int cols = rows.nth(i).locator("xpath=./td").count();
            if (cols > maxCols) {
                maxCols = cols;
            }
        }
        return maxCols;
    }

    // ========== Show Equal Rows ==========

    public void setShowEqualRows(boolean value) {
        if (showEqualRowsCheckbox.isChecked() != value) {
            showEqualRowsCheckbox.click();
            WaitUtil.sleep(500, "Waiting for equal rows filter to apply");
        }
    }

    public boolean isShowEqualRowsCheckboxVisible() {
        return showEqualRowsCheckbox.isVisible(2000);
    }

    // ========== Text files compare form ==========

    public boolean isCompareTextFilesFormClear() {
        if (textDiffSections.size() != 2) {
            return false;
        }
        boolean firstHasRows = textDiffSections.get(0).getLocator().locator("xpath=.//tr").count() > 0;
        boolean secondHasRows = textDiffSections.get(1).getLocator().locator("xpath=.//tr").count() > 0;
        return firstHasRows && secondHasRows;
    }

    public void close() {
        if (comparePopup != null && !comparePopup.isClosed()) {
            comparePopup.close();
        }
    }
}
