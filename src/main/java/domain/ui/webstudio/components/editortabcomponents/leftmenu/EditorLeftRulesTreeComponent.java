package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditorLeftRulesTreeComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(EditorLeftRulesTreeComponent.class);

    private WebElement viewFilterLink;
    private WebElement selectedTreeItem;
    private WebElement filterOptionTemplate;
    private WebElement tableIconTemplate;
    private List<EditorTreeFolderComponent> folders;
    private List<WebElement> categoryLabels;
    private List<WebElement> leafNodes;
    private WebElement advancedFilterBtn;
    private WebElement hideUtilityTablesCheckbox;
    private WebElement applyAdvancedFilterBtn;
    private WebElement closeAdvancedFilterBtn;

    public EditorLeftRulesTreeComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorLeftRulesTreeComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
        selectedTreeItem = createScopedElement("xpath=.//div[@id='rulesTree']//div[contains(@class,'rf-trn') and contains(@class,'sel')]//a", "selectedTreeItem");
        filterOptionTemplate = createScopedElement("xpath=.//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']", "filterOptionLink");
        tableIconTemplate = createScopedElement("xpath=.//div//a//span[text()='%s']/parent::*/parent::*/parent::*//img", "tableIconTemplate");
        folders = createScopedComponentList(EditorTreeFolderComponent.class, "xpath=(.//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]) | (.//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]) | (.//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')])", "treeFolders");
        categoryLabels = createScopedElementList("xpath=.//div[@id='rulesTree']//span[@class='rf-trn-lbl']/span", "categoryLabels");
        leafNodes = createScopedElementList("xpath=.//div[div/span[@class='rf-trn-hnd-lf rf-trn-hnd']]", "leafNodes");
        advancedFilterBtn = createScopedElement("xpath=.//div[@id='rulesTreeViewForm:rulesTreeView']//a[@title='Filter']", "advancedFilterBtn");
        hideUtilityTablesCheckbox = new WebElement(page, "xpath=//input[@id='filterForm:hideUtilityTables']", "hideUtilityTablesCheckbox");
        applyAdvancedFilterBtn = new WebElement(page, "xpath=//form[@id='filterForm']//input[@value='Apply']", "applyAdvancedFilterBtn");
        closeAdvancedFilterBtn = new WebElement(page, "xpath=//div[@id='modalTreeFilter_header_controls']//img[@class='close']", "closeAdvancedFilterBtn");
    }

    public String getViewFilterValue() {
        waitUntilSpinnerLoaded();
        return viewFilterLink.getText();
    }

    public List<String> getCategoriesVisible() {
        waitUntilSpinnerLoaded();
        return categoryLabels.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<String> getAllEndNodesNames() {
        waitUntilSpinnerLoaded();
        return leafNodes.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public EditorLeftRulesTreeComponent setAdvancedFilter(boolean hideUtilityTables) {
        advancedFilterBtn.click();
        if (hideUtilityTables != hideUtilityTablesCheckbox.isChecked()) {
            hideUtilityTablesCheckbox.click();
        }
        applyAdvancedFilterBtn.click();
        return this;
    }

    public boolean isHideUtilityTables() {
        advancedFilterBtn.click();
        boolean value = hideUtilityTablesCheckbox.isChecked();
        closeAdvancedFilterBtn.click();
        return value;
    }

    public EditorLeftRulesTreeComponent setViewFilter(FilterOptions filterOption) {
        waitUntilSpinnerLoaded();
        WaitUtil.sleep(1000, "Huge sleep just to make sure viewFilterLink is accessible");
        WaitUtil.waitForCondition(() -> {
            WaitUtil.waitForCondition(() -> {
                try {
                    WaitUtil.sleep(250, "Waiting before clicking view filter link");
                    viewFilterLink.click();
                    WaitUtil.sleep(250, "Waiting for filter dropdown to appear");
                } catch (Exception ignored) {}
                return filterOptionTemplate.format(filterOption.getValue()).isVisible();
            }, 5000, 200, "Waiting for filter option '" + filterOption.getValue() + "' to be visible");
            try {
                filterOptionTemplate.format(filterOption.getValue()).click();
            } catch (Exception ignored) {}
            return viewFilterLink.getText().toLowerCase().contains(filterOption.getValue().toLowerCase());
        }, 15000, 200, "Waiting for filter '" + filterOption.getValue() + "' to be applied");
        return this;
    }

    public EditorLeftRulesTreeComponent selectItemInFolder(String folderName, String itemName) {
        waitUntilSpinnerLoaded();
        EditorTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public EditorLeftRulesTreeComponent expandFolderInTree(String folderName) {
        waitUntilSpinnerLoaded();
        EditorTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    public boolean isItemExistsInTree(String itemName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getItem(itemName).isVisible());
    }

    public boolean isFolderExistsInTree(String folderName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getFolderName().equals(folderName));
    }

    public String getSelectedItemText() {
        if (selectedTreeItem.isVisible(1500)) {
            return selectedTreeItem.getText();
        }
        return "";
    }

    public void checkRulesTablePresent(String folderName, String tableName) {
        if (!isItemExistsInFolder(folderName, tableName)) {
            throw new AssertionError(String.format("Table '%s' not found in folder '%s'", tableName, folderName));
        }
    }

    public void checkRulesTableAbsent(String folderName, String tableName) {
        if (!isItemNotExistsInFolder(folderName, tableName)) {
            throw new AssertionError(String.format("Table '%s' should not exist in folder '%s'", tableName, folderName));
        }
    }

    public boolean isItemExistsInFolder(String folderName, String itemName) {
        return WaitUtil.waitForCondition(() -> {
            try {
                EditorTreeFolderComponent folder = findFolderInTree(folderName);
                return folder.getItem(itemName).isVisible();
            } catch (RuntimeException e) {
                return false;
            }
        }, DEFAULT_TIMEOUT_MS, 100, "Checking if item '" + itemName + "' exists in folder '" + folderName + "'");
    }

    public boolean isItemNotExistsInFolder(String folderName, String itemName) {
        return WaitUtil.waitForCondition(() -> {
            try {
                EditorTreeFolderComponent folder = findFolderInTree(folderName);
                return !folder.getItem(itemName).isVisible();
            } catch (RuntimeException e) {
                return true;
            }
        }, DEFAULT_TIMEOUT_MS, 100, "Checking if item '" + itemName + "' does NOT exist in folder '" + folderName + "'");
    }

    // Find specific folder in the tree by name
    private EditorTreeFolderComponent findFolderInTree(String folderName) {
        Optional<EditorTreeFolderComponent> result = WaitUtil.waitForResult(() -> findTreeFolders().stream()
                        .filter(f -> folderName.equals(f.getFolderName()))
                        .findFirst(), DEFAULT_TIMEOUT_MS, 100,
                "Searching for folder '" + folderName + "' in editor tree"
        );

        return result.orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    // Find all tree folder components dynamically (replaces @FindAll annotation)
    private List<EditorTreeFolderComponent> findTreeFolders() {
        WaitUtil.waitForListNotEmpty(() -> folders, DEFAULT_TIMEOUT_MS, 100, "Waiting for editor tree folders to load");
        return folders;
    }

    public EditorLeftRulesTreeComponent selectItemInFolderByIndex(String folderName, String itemName, int index) {
        waitUntilSpinnerLoaded();
        EditorTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItemByIndex(itemName, index);
        return this;
    }

    public WebElement getTableIcon(String tableName) {
        return tableIconTemplate.format(tableName);
    }

    @Getter
    public enum FilterOptions {
        BY_TYPE("By Type"),
        BY_EXCEL_SHEET("By Excel Sheet"),
        BY_CATEGORY("By Category"),
        BY_CATEGORY_DETAILED("By Category Detailed"),
        BY_CATEGORY_INVERSED("By Category Inversed");

        private String value;

        FilterOptions(String value) {
            this.value = value;
        }
    }
}