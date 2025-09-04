package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class RepositoryTreeFolderComponent extends CoreComponent {

    private WebElement expanderClosed;
    private WebElement folderName;
    private WebElement itemTemplate;

    public RepositoryTreeFolderComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryTreeFolderComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        expanderClosed = createScopedElement("xpath=.//span[contains(@class,'rf-trn-hnd-colps')]", "expanderClosed");
        folderName = createScopedElement("xpath=./div/span/span[text() or ./span[text()]]", "folderName");
        itemTemplate = createScopedElement("xpath=.//span[text()='%s']", "itemTemplate");
    }

    public WebElement getItem(String itemName) {
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