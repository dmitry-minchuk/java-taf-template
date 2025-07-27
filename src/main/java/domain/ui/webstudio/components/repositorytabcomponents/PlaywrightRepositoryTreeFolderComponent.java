package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoryTreeFolderComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement folderIcon;
    private PlaywrightWebElement folderName;
    private PlaywrightWebElement expandIcon;
    private PlaywrightWebElement collapseIcon;

    public PlaywrightRepositoryTreeFolderComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryTreeFolderComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        folderIcon = createScopedElement(".//span[contains(@class,'folder-icon')]", "folderIcon");
        folderName = createScopedElement(".//span[contains(@class,'folder-name')]", "folderName");
        expandIcon = createScopedElement(".//span[contains(@class,'expand-icon')]", "expandIcon");
        collapseIcon = createScopedElement(".//span[contains(@class,'collapse-icon')]", "collapseIcon");
    }

    public void clickFolder() {
        folderName.click();
    }

    public void expandFolder() {
        if (expandIcon.isVisible()) {
            expandIcon.click();
        }
    }

    public void collapseFolder() {
        if (collapseIcon.isVisible()) {
            collapseIcon.click();
        }
    }

    public String getFolderName() {
        return folderName.getText();
    }

    public boolean isFolderExpanded() {
        return collapseIcon.isVisible();
    }

    public boolean isFolderCollapsed() {
        return expandIcon.isVisible();
    }
}