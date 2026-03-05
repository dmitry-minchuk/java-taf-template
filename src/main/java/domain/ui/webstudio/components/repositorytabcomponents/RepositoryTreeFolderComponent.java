package domain.ui.webstudio.components.repositorytabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

public class RepositoryTreeFolderComponent extends BaseComponent {

    private WebElement expanderClosed;
    private WebElement folderName;
    private WebElement itemTemplate;
    // Use closed/opened item templates ONLY when duplicate project names exist in the tree
    // (one opened, one closed) — otherwise use the regular itemTemplate
    private WebElement closedItemTemplate;
    private WebElement openedItemTemplate;

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
        // Target the inner span with id that contains the actual text node
        // This avoids matching parent containers and handles both plain text and nested spans
        itemTemplate = createScopedElement("xpath=.//span[@class='rf-trn-lbl']/span[starts-with(@id, 'projectTree:') and normalize-space(text()[1])='%s']", "itemTemplate");
        closedItemTemplate = createScopedElement("xpath=.//div[.//img[contains(@src,'closed.gif')]]//span[@class='rf-trn-lbl']/span[starts-with(@id, 'projectTree:') and normalize-space(text()[1])='%s']", "closedItemTemplate");
        openedItemTemplate = createScopedElement("xpath=.//div[.//img[contains(@src,'opened.gif')]]//span[@class='rf-trn-lbl']/span[starts-with(@id, 'projectTree:') and normalize-space(text()[1])='%s']", "openedItemTemplate");
    }

    public WebElement getItem(String itemName) {
        return itemTemplate.format(itemName);
    }

    public void expandFolder() {
        if (WaitUtil.waitForCondition(() -> expanderClosed.isVisible(), 500, 100, "Waiting for Folder expander to be visible"))
            expanderClosed.click();
    }

    public void selectItem(String itemName) {
        getItem(itemName).click();
    }

    public WebElement getClosedItem(String itemName) {
        return closedItemTemplate.format(itemName);
    }

    public void selectClosedItem(String itemName) {
        getClosedItem(itemName).click();
    }

    public WebElement getOpenedItem(String itemName) {
        return openedItemTemplate.format(itemName);
    }

    public void selectOpenedItem(String itemName) {
        getOpenedItem(itemName).click();
    }

    public String getFolderName() {
        return folderName.getText(500);
    }
}