package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

import java.util.List;

public class RepositoryContentTabSwitcherComponent extends BaseComponent {

    private WebElement propertiesTab;
    private WebElement revisionsTab;
    private WebElement elementsTab;
    private WebElement deployConfigTab;

    private RepositoryContentTabPropertiesComponent propertiesTabComponent;
    private RepositoryContentRevisionsTabComponent revisionsTabComponent;
    private ElementsTabComponent elementsTabComponent;
    private DeployConfigurationTabsComponent deployConfigTabComponent;

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

        propertiesTabComponent = createScopedComponent(RepositoryContentTabPropertiesComponent.class, "xpath=.//div[@id='properties']", "propertiesTabComponent");
        revisionsTabComponent = createScopedComponent(RepositoryContentRevisionsTabComponent.class, "xpath=.//div[@id='revisions']", "revisionsTabComponent");
        elementsTabComponent = createScopedComponent(ElementsTabComponent.class, "xpath=.//div[@id='elements']", "elementsTabComponent");
        deployConfigTabComponent = createScopedComponent(DeployConfigurationTabsComponent.class, "xpath=.//div[@id='content']", "deployConfigTabComponent");
    }

    public RepositoryContentTabPropertiesComponent selectPropertiesTab() {
        propertiesTab.click();
        return propertiesTabComponent;
    }

    public RepositoryContentRevisionsTabComponent selectRevisionsTab() {
        revisionsTab.click();
        return revisionsTabComponent;
    }

    public ElementsTabComponent selectElementsTab() {
        elementsTab.click();
        return elementsTabComponent;
    }

    public DeployConfigurationTabsComponent selectDeployConfigTab() {
        deployConfigTab.click();
        return deployConfigTabComponent;
    }

    public List<String> getAvailableTabNames() {
        return rootLocator.getLocator().locator("xpath=.//td[@data-tabname]")
                .allTextContents()
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
