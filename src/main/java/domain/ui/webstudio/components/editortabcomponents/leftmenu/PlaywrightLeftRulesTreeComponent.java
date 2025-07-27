package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

// Playwright version of LeftRulesTreeComponent for rules tree navigation and filtering
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
        viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
        viewFilterOptionsLink = createScopedElement("xpath=.//ul[@class='dropdown-menu link-dropdown-menu']/li/a[text()='%s']", "viewFilterOptionsLink");
        selectedTreeItem = createScopedElement("xpath=.//div[@id='rulesTree']//div[contains(@class,'rf-trn') and contains(@class,'sel')]//a", "selectedTreeItem");
        initializeTreeFolderComponents();
    }

    private void initializeTreeFolderComponents() {
        treeFolderComponentList = new java.util.ArrayList<>();
    }

    public PlaywrightLeftRulesTreeComponent setViewFilter(FilterOptions filterOption) {
        viewFilterLink.click();
        viewFilterOptionsLink.format(filterOption.getValue()).click();
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
        String selector = ".//div[@id='rulesTree']//div[contains(@class, 'rf-tr-nd') and (.//span[contains(@class,'rf-trn-hnd')])]";
        try {
            WaitUtil.sleep(300);
            int folderCount = page.locator(String.format("xpath=%s", selector)).count();
            return IntStream.range(0, folderCount)
                    .mapToObj(i -> new PlaywrightTreeFolderComponent(
                            new PlaywrightWebElement(page, 
                                String.format("xpath=(%s)[%d]", selector, i + 1), 
                                String.format("treeFolderElement_%d", i))))
                    .toList();
        } catch (Exception e) {
            LOGGER.debug("No tree folders found: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
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