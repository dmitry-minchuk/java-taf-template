package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Page;
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
    private WebElement treeContainer;
    // Template: %s = nodeName
    private WebElement treeNodeExpanderTemplate;
    // Template: %s = nodeName
    private WebElement treeNodeLinkTemplate;
    // Template: %s = idSuffix e.g. "1_te_c-7:5"
    private WebElement cellTemplate;
    // Template: %s = fragment number
    private WebElement editorRowsTemplate;
    private Page comparePopup;

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
        treeContainer = new WebElement(getPage(), "xpath=//span[@class='rf-trn-cnt']", "treeContainer");
        treeNodeExpanderTemplate = new WebElement(getPage(),
                "xpath=//div[@class='rf-trn' and .//span[contains(@class,'rf-trn-lbl') and (text()='%s')]]/span[1]",
                "treeNodeExpanderTemplate");
        treeNodeLinkTemplate = new WebElement(getPage(),
                "xpath=//span[contains(@class,'rf-trn-lbl') and (text()='%s')]",
                "treeNodeLinkTemplate");
        // idSuffix pattern: "1_te_c-7:5" — uses ends-with to avoid substring collisions (e.g. c-2:1 vs c-2:10)
        cellTemplate = new WebElement(getPage(),
                "xpath=//td[(contains(@id,'diffTreeForm') or contains(@id,'compareRevisionsForm')) and (substring(@id, string-length(@id) - string-length('%1$s') + 1) = '%1$s')]",
                "cellTemplate");
        editorRowsTemplate = new WebElement(getPage(),
                "xpath=//div[@id='diffTreeForm:editor%s_te_table']//tr[./td]",
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
        List<String> options = revisionSelect.getSelectVisibleTextValues();
        if (options.size() > 1) {
            revisionSelect.getLocator().selectOption(new com.microsoft.playwright.options.SelectOption().setIndex(index));
        }
    }

    public void clickCompareBtn() {
        compareBtnInPopup.waitForVisible(5000);
        compareBtnInPopup.click();
        WaitUtil.sleep(1000, "Waiting for comparison results to load");
        if (!treeContainer.isVisible(3000)) {
            compareBtnInPopup.click();
            WaitUtil.sleep(1000, "Retry waiting for comparison tree");
        }
    }

    // ========== Tree navigation ==========

    public void openTreeNode(String nodeName) {
        treeNodeExpanderTemplate.format(nodeName).waitForVisible(5000);
        treeNodeExpanderTemplate.format(nodeName).click();
        WaitUtil.sleep(500, "Waiting for repo tree node to expand: " + nodeName);
    }

    public void clickTreeNode(String nodeName) {
        treeNodeLinkTemplate.format(nodeName).waitForVisible(3000);
        treeNodeLinkTemplate.format(nodeName).click();
        WaitUtil.sleep(500, "Waiting after clicking repo tree node: " + nodeName);
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
