package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightLeftRepositoryTreeComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement repositoryTree;
    private PlaywrightWebElement treeNode;
    private PlaywrightWebElement expandBtn;
    private PlaywrightWebElement collapseBtn;

    public PlaywrightLeftRepositoryTreeComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightLeftRepositoryTreeComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        repositoryTree = new PlaywrightWebElement(page, ".//div[contains(@class,'repository-tree')]", "Repository Tree");
        treeNode = new PlaywrightWebElement(page, ".//div[contains(@class,'tree-node')]", "Tree Node");
        expandBtn = new PlaywrightWebElement(page, ".//span[contains(@class,'expand')]", "Expand Button");
        collapseBtn = new PlaywrightWebElement(page, ".//span[contains(@class,'collapse')]", "Collapse Button");
    }

    public void expandNode() {
        expandBtn.click();
    }

    public void collapseNode() {
        collapseBtn.click();
    }

    public void selectNode(String nodeName) {
        page.locator(".//div[contains(@class,'tree-node') and contains(text(),'" + nodeName + "')]").click();
    }

    public boolean isRepositoryTreeVisible() {
        return repositoryTree.isVisible();
    }
}