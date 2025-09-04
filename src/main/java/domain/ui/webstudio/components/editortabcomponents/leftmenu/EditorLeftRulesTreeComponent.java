package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class EditorLeftRulesTreeComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(EditorLeftRulesTreeComponent.class);

    private WebElement viewFilterLink;
    private WebElement selectedTreeItem;
    private WebElement filterOptionTemplate;

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
    }

    public EditorLeftRulesTreeComponent setViewFilter(FilterOptions filterOption) {
        if(!viewFilterLink.getText().toLowerCase().contains(filterOption.getValue().toLowerCase())) {
            while(!filterOptionTemplate.format(filterOption.getValue()).isVisible(200)) {
                WaitUtil.sleep(250);
                try {
                    viewFilterLink.click();
                } catch (Exception ignored) {}
                WaitUtil.sleep(250);
            }
            filterOptionTemplate.format(filterOption.getValue()).click();
        }
        return this;
    }

    public EditorLeftRulesTreeComponent selectItemInFolder(String folderName, String itemName) {
        EditorTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public EditorLeftRulesTreeComponent expandFolderInTree(String folderName) {
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
        if (isItemExistsInFolder(folderName, tableName)) {
            throw new AssertionError(String.format("Table '%s' should not exist in folder '%s'", tableName, folderName));
        }
    }

    public boolean isItemExistsInFolder(String folderName, String itemName) {
        try {
            EditorTreeFolderComponent folder = findFolderInTree(folderName);
            return folder.getItem(itemName).isVisible();
        } catch (RuntimeException e) {
            return false;
        }
    }


    // Find specific folder in the tree by name
    private EditorTreeFolderComponent findFolderInTree(String folderName) {
        return findTreeFolders().stream()
                .filter(folder -> folder.getFolderName().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    // Find all tree folder components dynamically (replaces @FindAll annotation)
    private List<EditorTreeFolderComponent> findTreeFolders() {
        List<EditorTreeFolderComponent> folders = new java.util.ArrayList<>();
        
        // Wait for the tree to be loaded
        WaitUtil.sleep(250);

        String[] selectors = {
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]",
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]",
                ".//div[@id='rulesTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]"
        };

        for (String selector : selectors) {
            int folderCount = page.locator("xpath=" + selector).count();
            for (int i = 0; i < folderCount; i++) {
                String componentName = String.format("treeFolderElement_%d_%d", folders.size(), i);
                WebElement indexedSelectorTemplate = createScopedElement("xpath=(%s)[%d]", componentName);
                WebElement indexedElement = indexedSelectorTemplate.format(selector, i + 1);
                EditorTreeFolderComponent folder = createScopedComponent(EditorTreeFolderComponent.class, indexedElement);
                if(folder.isVisible())
                    folders.add(folder);
            }
        }

        LOGGER.debug("Found {} tree folders", folders.size());
        List<String> folderNames = folders.stream().map(EditorTreeFolderComponent::getFolderName).toList();
        folderNames.forEach(LOGGER::debug);
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