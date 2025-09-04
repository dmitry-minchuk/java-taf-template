package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LeftRepositoryTreeComponent extends CoreComponent {

    private static final Logger LOGGER = LogManager.getLogger(LeftRepositoryTreeComponent.class);

    public LeftRepositoryTreeComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public LeftRepositoryTreeComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
    }

    public LeftRepositoryTreeComponent selectItemInFolder(String folderName, String itemName) {
        PlaywrightRepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public LeftRepositoryTreeComponent expandFolderInTree(String folderName) {
        PlaywrightRepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    public boolean isItemExistsInTree(String itemName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getItem(itemName).isVisible(100));
    }

    public boolean isFolderExistsInTree(String folderName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getFolderName().equals(folderName));
    }

    // Compatibility method for test migration
    public void selectProjectInTree(String projectName) {
        selectItemInFolder("Projects", projectName);
    }

    // Find specific folder in the tree by name
    private PlaywrightRepositoryTreeFolderComponent findFolderInTree(String folderName) {
        return findTreeFolders().stream()
                .filter(folder -> folder.getFolderName().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    // Find all tree folder components dynamically (replaces @FindAll annotation)
    private List<PlaywrightRepositoryTreeFolderComponent> findTreeFolders() {
        List<PlaywrightRepositoryTreeFolderComponent> folders = new java.util.ArrayList<>();
        WaitUtil.sleep(1000); //needed here for deploy freezing operations

        String[] selectors = {
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]",
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]",
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]"
        };

        for (String selector : selectors) {
            int folderCount = page.locator("xpath=" + selector).count();
            for (int i = 0; i < folderCount; i++) {
                String componentName = String.format("treeFolderElement_%d_%d", folders.size(), i);
                WebElement indexedSelectorTemplate = createScopedElement("xpath=(%s)[%d]", componentName);
                WebElement indexedElement = indexedSelectorTemplate.format(selector, i + 1);
                PlaywrightRepositoryTreeFolderComponent folder = createScopedComponent(PlaywrightRepositoryTreeFolderComponent.class, indexedElement);
                if(folder.isVisible())
                    folders.add(folder);
            }
        }

        LOGGER.debug("Found {} tree folders", folders.size());
        List<String> folderNames = folders.stream().map(PlaywrightRepositoryTreeFolderComponent::getFolderName).toList();
        folderNames.forEach(LOGGER::debug);
        return folders;
    }
}