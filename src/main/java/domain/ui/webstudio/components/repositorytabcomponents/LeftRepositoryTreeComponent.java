package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.BasePageComponent;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LeftRepositoryTreeComponent extends BasePageComponent {

    @FindAll({
        @FindBy(xpath = ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-colps')]"),
        @FindBy(xpath = ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-colps')] and contains(@class, 'rf-tr-nd-exp')]"),
        @FindBy(xpath = ".//div[@id='projectTree']//div[./div/span[contains(@class,'rf-trn-hnd-exp')] and contains(@class, 'rf-tr-nd-exp')]")
    }) //Looking for all folders (even nested) - so actions on nested ones will depend on ancestor-folders
    private List<RepositoryTreeFolderComponent> treeFolderComponentList;

    public LeftRepositoryTreeComponent selectItemInFolder(String folderName, String itemName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.selectItem(itemName);
        return this;
    }

    public LeftRepositoryTreeComponent expandFolderInTree(String folderName) {
        RepositoryTreeFolderComponent folder = findFolderInTree(folderName);
        folder.expandFolder();
        return this;
    }

    public boolean isItemExistsInTree(String itemName) {
        return treeFolderComponentList.stream().anyMatch(c -> c.getItem(itemName).isDisplayed(1));
    }

    public boolean isFolderExistsInTree(String folderName) {
        return treeFolderComponentList.stream().
                anyMatch(c -> c.getFolderName().getText().equals(folderName));
    }

    private RepositoryTreeFolderComponent findFolderInTree(String folderName) {
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - use proper element waiting in component initialization
        return treeFolderComponentList.stream()
                .filter(c -> c.getFolderName().getText().equals(folderName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Folder with name %s not found", folderName)));
    }
} 