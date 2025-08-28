package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightTreeFolderComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlaywrightLeftRepositoryTreeComponent extends PlaywrightBasePageComponent {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightLeftRepositoryTreeComponent.class);

    public PlaywrightLeftRepositoryTreeComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftRepositoryTreeComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
    }

    public PlaywrightLeftRepositoryTreeComponent selectItemInFolder(String folderName, String itemName) {
        PlaywrightRepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public PlaywrightLeftRepositoryTreeComponent expandFolderInTree(String folderName) {
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
        WaitUtil.sleep(250);

        String[] selectors = {
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]",
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]",
                ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]"
        };

        for (String selector : selectors) {
            int folderCount = page.locator("xpath=" + selector).count();
            for (int i = 0; i < folderCount; i++) {
                String componentName = String.format("treeFolderElement_%d_%d", folders.size(), i);
                PlaywrightWebElement indexedSelectorTemplate = createScopedElement("xpath=(%s)[%d]", componentName);
                PlaywrightWebElement indexedElement = indexedSelectorTemplate.format(selector, i + 1);
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