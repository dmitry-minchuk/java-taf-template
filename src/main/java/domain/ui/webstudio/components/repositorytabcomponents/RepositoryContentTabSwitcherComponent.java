package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class RepositoryContentTabSwitcherComponent extends BaseComponent {

    private WebElement propertiesTab;
    private WebElement revisionsTab;
    private WebElement elementsTab;
    private WebElement deployConfigTab;

    public RepositoryContentTabSwitcherComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentTabSwitcherComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        propertiesTab = createScopedElement("xpath=.//td[@data-tabname='Properties']", "propertiesTab");
        revisionsTab = createScopedElement("xpath=.//td[@data-tabname='Revisions']", "revisionsTab");
        elementsTab = createScopedElement("xpath=.//td[@data-tabname='Elements']", "elementsTab");
        deployConfigTab = createScopedElement("xpath=.//td[@data-tabname='RulesDeployConfiguration']", "deployConfigTab");
    }

    public RepositoryContentTabSwitcherComponent selectPropertiesTab() {
        propertiesTab.click();
        return this;
    }

    public RepositoryContentTabSwitcherComponent selectRevisionsTab() {
        revisionsTab.click();
        return this;
    }

    public RepositoryContentTabSwitcherComponent selectElementsTab() {
        elementsTab.click();
        return this;
    }

    public RepositoryContentTabSwitcherComponent selectDeployConfigTab() {
        deployConfigTab.click();
        return this;
    }

    public boolean isPropertiesTabVisible() {
        return propertiesTab.isVisible();
    }

    public boolean isRevisionsTabVisible() {
        return revisionsTab.isVisible();
    }

    public boolean isElementsTabVisible() {
        return elementsTab.isVisible();
    }

    public boolean isDeployConfigTabVisible() {
        return deployConfigTab.isVisible();
    }
}
