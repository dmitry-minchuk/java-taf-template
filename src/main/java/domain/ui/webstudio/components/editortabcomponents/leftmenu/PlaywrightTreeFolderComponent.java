package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Handles folder expansion and item selection within the rules tree
public class PlaywrightTreeFolderComponent extends PlaywrightBasePageComponent {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightTreeFolderComponent.class);
    
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
        // Use createScopedElement to properly inherit component scoping context
        expanderClosed = createScopedElement("xpath=.//span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        folderName = createScopedElement("xpath=.//span[@class='rf-trn-lbl']/span", "folderName");
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