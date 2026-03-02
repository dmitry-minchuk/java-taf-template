package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Handles folder expansion and item selection within the rules tree
public class EditorTreeFolderComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(EditorTreeFolderComponent.class);
    
    private WebElement expanderClosed;
    private WebElement folderName;
    private WebElement itemTemplate;
    private WebElement indexedItemTemplate;

    public EditorTreeFolderComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorTreeFolderComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        expanderClosed = createScopedElement("xpath=./div/span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        folderName = createScopedElement("xpath=./div/span/span/span[text()][1]", "folderName");
        itemTemplate = createScopedElement("xpath=.//a[span[text()='%s']]", "treeItem");
        indexedItemTemplate = createScopedElement("xpath=(.//a[span[text()='%s']])[%s]", "indexedTreeItem");
    }

    public void expandFolder() {
        WaitUtil.waitForCondition(() ->{
            if (expanderClosed.isVisible()) {
                expanderClosed.click();
                return true;
            } else
                return false;
        }, 1000, 100, "Waiting for folder expander to be visible and expanding tree folder");
    }

    public String getFolderName() {
        if(folderName.isVisible())
            return folderName.getText();
        else
            return null;
    }

    public void selectItem(String itemName) {
        itemTemplate.format(itemName).click();
    }

    public WebElement getItem(String itemName) {
        return itemTemplate.format(itemName);
    }

    public void selectItemByIndex(String itemName, int index) {
        indexedItemTemplate.format(itemName, index).click();
    }
}