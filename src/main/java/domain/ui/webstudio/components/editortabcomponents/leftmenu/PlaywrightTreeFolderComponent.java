package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

/**
 * Playwright version of TreeFolderComponent for individual folder operations
 * Handles folder expansion and item selection within the rules tree
 */
public class PlaywrightTreeFolderComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement expanderClosed;
    @Getter
    private PlaywrightWebElement folderName;
    private PlaywrightWebElement item;

    public PlaywrightTreeFolderComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTreeFolderComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Expander when closed: ".//span[contains(@class,'rf-trn-hnd-colps')]"
        expanderClosed = createScopedElement("xpath=.//span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        
        // Folder name: ".//span/span/span"
        folderName = createScopedElement("xpath=.//span/span/span", "folderName");
        
        // Item within folder: ".//a[span[text()='%s']]"
        item = createScopedElement("xpath=.//a[span[text()='%s']]", "item");
    }

    public void expandFolder() {
        if (expanderClosed.isVisible()) {
            expanderClosed.click();
        }
    }

    public void selectItem(String itemName) {
        item.format(itemName).click();
    }

    public PlaywrightWebElement getItem(String itemName) {
        return item.format(itemName);
    }

}