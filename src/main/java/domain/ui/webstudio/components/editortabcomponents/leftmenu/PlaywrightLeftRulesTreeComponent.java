package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

// Playwright version of LeftRulesTreeComponent for rules tree navigation and filtering
public class PlaywrightLeftRulesTreeComponent extends PlaywrightBasePageComponent {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightLeftRulesTreeComponent.class);

    private List<PlaywrightTreeFolderComponent> treeFolderComponentList;
    private PlaywrightWebElement viewFilterLink;
    private PlaywrightWebElement selectedTreeItem;
    private PlaywrightWebElement filterOptionTemplate;

    public PlaywrightLeftRulesTreeComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftRulesTreeComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
        selectedTreeItem = createScopedElement("xpath=.//div[@id='rulesTree']//div[contains(@class,'rf-trn') and contains(@class,'sel')]//a", "selectedTreeItem");
        filterOptionTemplate = createScopedElement("xpath=.//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']", "filterOptionLink");
        
        initializeTreeFolderComponents();
    }

    private void initializeTreeFolderComponents() {
        treeFolderComponentList = new java.util.ArrayList<>();
    }

    public PlaywrightLeftRulesTreeComponent setViewFilter(FilterOptions filterOption) {
        do {
            viewFilterLink.click();
            filterOptionTemplate.format(filterOption.getValue()).click();
        } while(!viewFilterLink.getText().toLowerCase().contains(filterOption.getValue().toLowerCase()));
        return this;
    }

    public PlaywrightLeftRulesTreeComponent selectItemInFolder(String folderName, String itemName) {
        PlaywrightTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public PlaywrightLeftRulesTreeComponent expandFolderInTree(String folderName) {
        PlaywrightTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    public boolean isItemExistsInTree(String itemName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getItem(itemName).isVisible());
    }

    public boolean isFolderExistsInTree(String folderName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getFolderName().getText().equals(folderName));
    }

    public String getSelectedItemText() {
        if (selectedTreeItem.isVisible()) {
            return selectedTreeItem.getText();
        }
        return "";
    }

    // Find specific folder in the tree by name
    private PlaywrightTreeFolderComponent findFolderInTree(String folderName) {
        return findTreeFolders().stream()
                .filter(folder -> folder.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    // Find all tree folder components dynamically (replaces @FindAll annotation)
    private List<PlaywrightTreeFolderComponent> findTreeFolders() {
        List<PlaywrightTreeFolderComponent> folders = new java.util.ArrayList<>();
        
        // Wait for the tree to be loaded
        page.waitForSelector("xpath=.//div[@id='rulesTree']", new Page.WaitForSelectorOptions().setTimeout(5000));

        String[] selectors = {
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]",
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]",
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]"
        };

        for (String selector : selectors) {
            int folderCount = page.locator("xpath=" + selector).count();
            for (int i = 0; i < folderCount; i++) {
                String componentName = String.format("treeFolderElement_%d_%d", folders.size(), i);
                PlaywrightWebElement indexedSelectorTemplate = createScopedElement("xpath=(%s)[%d]", componentName);
                PlaywrightWebElement indexedElement = indexedSelectorTemplate.format(selector, i + 1);
                PlaywrightTreeFolderComponent folder = createScopedComponent(
                        PlaywrightTreeFolderComponent.class, 
                        indexedElement);
                folders.add(folder);
            }
        }

        LOGGER.debug("Found {} tree folders", folders.size());
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