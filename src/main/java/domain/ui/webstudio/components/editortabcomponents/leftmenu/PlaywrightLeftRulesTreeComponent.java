package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Playwright version of LeftRulesTreeComponent for rules tree navigation and filtering
 * Supports view filtering, folder expansion, and item selection with native Playwright waits
 */
public class PlaywrightLeftRulesTreeComponent extends PlaywrightBasePageComponent {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightLeftRulesTreeComponent.class);
    
    private List<PlaywrightTreeFolderComponent> treeFolderComponentList;
    private PlaywrightWebElement viewFilterLink;
    private PlaywrightWebElement viewFilterOptionsLink;
    private PlaywrightWebElement selectedTreeItem;

    public PlaywrightLeftRulesTreeComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftRulesTreeComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // View filter link: ".//div[@class='filter-view']/span/a"
        viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
        
        // View filter options: ".//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']"
        viewFilterOptionsLink = createScopedElement("xpath=.//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']", "viewFilterOptionsLink");
        
        // Selected tree item: ".//div[@id='rulesTree']//div[contains(@class,'rf-trn') and contains(@class,'sel')]//a"
        selectedTreeItem = createScopedElement("xpath=.//div[@id='rulesTree']//div[contains(@class,'rf-trn') and contains(@class,'sel')]//a", "selectedTreeItem");
        
        // Tree folder components list - complex selector matching original
        initializeTreeFolderComponents();
    }

    private void initializeTreeFolderComponents() {
        // For now, we'll initialize this dynamically when needed
        // The original uses @FindAll with multiple selectors for collapsed/expanded folders
        treeFolderComponentList = new java.util.ArrayList<>();
    }

    /**
     * Set view filter for the rules tree
     * @param filterOption Filter option to apply (BY_CATEGORY, BY_TYPE, etc.)
     * @return This component for method chaining
     */
    public PlaywrightLeftRulesTreeComponent setViewFilter(FilterOptions filterOption) {
        viewFilterLink.click();
        viewFilterOptionsLink.format(filterOption.getValue()).click();
        return this;
    }

    /**
     * Select an item within a specific folder
     * @param folderName Name of the folder containing the item
     * @param itemName Name of the item to select
     * @return This component for method chaining
     */
    public PlaywrightLeftRulesTreeComponent selectItemInFolder(String folderName, String itemName) {
        PlaywrightTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    /**
     * Expand a specific folder in the tree
     * @param folderName Name of the folder to expand
     * @return This component for method chaining
     */
    public PlaywrightLeftRulesTreeComponent expandFolderInTree(String folderName) {
        PlaywrightTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    /**
     * Check if an item exists in the tree
     * @param itemName Name of the item to check
     * @return true if item exists and is visible
     */
    public boolean isItemExistsInTree(String itemName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getItem(itemName).isVisible());
    }

    /**
     * Check if a folder exists in the tree
     * @param folderName Name of the folder to check
     * @return true if folder exists
     */
    public boolean isFolderExistsInTree(String folderName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getFolderName().getText().equals(folderName));
    }

    /**
     * Get the text of the currently selected item
     * @return Text of selected item, or empty string if none selected
     */
    public String getSelectedItemText() {
        if (selectedTreeItem.isVisible()) {
            return selectedTreeItem.getText();
        }
        return "";
    }

    /**
     * Find a specific folder in the tree by name
     * @param folderName Name of the folder to find
     * @return PlaywrightTreeFolderComponent for the folder
     * @throws RuntimeException if folder not found
     */
    private PlaywrightTreeFolderComponent findFolderInTree(String folderName) {
        return findTreeFolders().stream()
                .filter(folder -> folder.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    /**
     * Find all tree folder components dynamically
     * This replaces the original @FindAll annotation approach
     * @return List of PlaywrightTreeFolderComponent
     */
    private List<PlaywrightTreeFolderComponent> findTreeFolders() {
        List<PlaywrightTreeFolderComponent> folders = new java.util.ArrayList<>();
        
        // Simplified approach: use a single selector that captures all folder types
        // This matches the essence of the original @FindAll behavior
        String combinedSelector = ".//div[@id='rulesTree']//div[" +
                "contains(@class, 'rf-tr-nd') and " +
                "(.//span[contains(@class,'rf-trn-hnd')])" +
                "]";
        
        // Use Playwright's ability to find all matching elements
        try {
            // Find count of matching elements first
            var folderLocator = page.locator("xpath=" + combinedSelector);
            int folderCount = folderLocator.count();
            
            // Create PlaywrightTreeFolderComponent for each found element
            for (int i = 0; i < folderCount; i++) {
                // Create element wrapper for each folder using nth selector
                String nthSelector = "xpath=(" + combinedSelector + ")[" + (i + 1) + "]";
                PlaywrightWebElement folderElement = new PlaywrightWebElement(
                    page, 
                    nthSelector, 
                    "treeFolderElement_" + i
                );
                folders.add(new PlaywrightTreeFolderComponent(folderElement));
            }
        } catch (Exception e) {
            // If no folders found, return empty list (like original would with no matches)
            LOGGER.debug("No tree folders found: {}", e.getMessage());
        }
        
        return folders;
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