package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoryTreeFolderComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement expanderClosed;
    private PlaywrightWebElement folderName;
    private PlaywrightWebElement itemTemplate;

    public PlaywrightRepositoryTreeFolderComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryTreeFolderComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        expanderClosed = createScopedElement("xpath=.//span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        folderName = createScopedElement("xpath=./div/span/span[text() or ./span[text()]]", "folderName");
        itemTemplate = createScopedElement("xpath=.//span[text()='%s']", "itemTemplate");
    }

    public PlaywrightWebElement getItem(String itemName) {
        return itemTemplate.format(itemName);
    }

    public void expandFolder() {
        if (expanderClosed.isVisible(200)) {
            expanderClosed.click();
        }
    }

    public void selectItem(String itemName) {
        getItem(itemName).click();
    }

    public String getFolderName() {
        return folderName.getText(500);
    }
}