package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class LeftRepositoryTreeComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(LeftRepositoryTreeComponent.class);
    private List<RepositoryTreeFolderComponent> folders;

    public LeftRepositoryTreeComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public LeftRepositoryTreeComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        folders = createScopedComponentList(RepositoryTreeFolderComponent.class, "xpath=(.//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]) | (.//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]) | (.//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')])", "treeFolders");
    }

    public LeftRepositoryTreeComponent selectItemInFolder(String folderName, String itemName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        WaitUtil.sleep(500, "Waiter for selected item details to load");
        return this;
    }

    // Use only when duplicate project names exist (one opened, one closed) to resolve strict mode violation
    public LeftRepositoryTreeComponent selectClosedItemInFolder(String folderName, String itemName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectClosedItem(itemName);
        WaitUtil.sleep(500, "Waiter for selected item details to load");
        return this;
    }

    // Use only when duplicate project names exist (one opened, one closed) to resolve strict mode violation
    public LeftRepositoryTreeComponent selectOpenedItemInFolder(String folderName, String itemName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectOpenedItem(itemName);
        WaitUtil.sleep(500, "Waiter for selected item details to load");
        return this;
    }

    public LeftRepositoryTreeComponent expandFolderInTree(String folderName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    public boolean isItemExistsInTree(String itemName) {
        return findTreeFolders().stream()
                .anyMatch(folder -> folder.getItem(itemName).isVisible(250));
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
    private RepositoryTreeFolderComponent findFolderInTree(String folderName) {
        Optional<RepositoryTreeFolderComponent> result = WaitUtil.waitForResult(() -> findTreeFolders().stream()
                .filter(f -> folderName.equals(f.getFolderName()))
                .findFirst(), DEFAULT_TIMEOUT_MS, 100,
                "Searching for folder '" + folderName + "' in repository tree"
        );

        return result.orElseThrow(() ->
                new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }

    // Find all tree folder components dynamically (replaces @FindAll annotation)
    private List<RepositoryTreeFolderComponent> findTreeFolders() {
        WaitUtil.waitForListNotEmpty(() -> folders, DEFAULT_TIMEOUT_MS, 100, "Waiting for repository tree folders to load");
        return folders;
    }
}