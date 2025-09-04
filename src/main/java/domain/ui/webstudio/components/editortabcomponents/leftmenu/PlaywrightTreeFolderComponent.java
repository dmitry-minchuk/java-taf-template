package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.LocalDriverPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Handles folder expansion and item selection within the rules tree
public class PlaywrightTreeFolderComponent extends CoreComponent {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightTreeFolderComponent.class);
    
    private PlaywrightWebElement expanderClosed;
    private PlaywrightWebElement folderName;
    private PlaywrightWebElement itemTemplate;

    public PlaywrightTreeFolderComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTreeFolderComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        expanderClosed = createScopedElement("xpath=./div/span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        folderName = createScopedElement("xpath=./div/span/span/span[text()][1]", "folderName");
        itemTemplate = createScopedElement("xpath=.//a[span[text()='%s']]", "treeItem");
    }

    public void expandFolder() {
        if (expanderClosed.isVisible()) {
            expanderClosed.click();
        }
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

    public PlaywrightWebElement getItem(String itemName) {
        return itemTemplate.format(itemName);
    }

}