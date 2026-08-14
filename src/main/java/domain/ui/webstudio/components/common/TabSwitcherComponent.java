package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.BasePage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

public class TabSwitcherComponent extends BaseComponent {

    private WebElement tabTemplate;
    private WebElement tabLabelTemplate;

    public TabSwitcherComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public TabSwitcherComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tabTemplate = createScopedElement("xpath=./li[./span[text()='%s']]", "selectedTab");
        tabLabelTemplate = createScopedElement("xpath=./li[./span[text()='%s']]/span", "selectedTabLabel");
    }

    public List<String> getVisibleTabNames() {
        return rootLocator.getLocator().locator("xpath=./li//span[not(*)]")
                .allInnerTexts().stream().map(String::trim).filter(name -> !name.isEmpty()).toList();
    }

    public boolean isTabOfferedWithin(String tabName, long timeoutMs) {
        return WaitUtil.waitForCondition(() -> getVisibleTabNames().contains(tabName),
                timeoutMs, 500, "Waiting for the '" + tabName + "' tab to be offered");
    }

    @SuppressWarnings("unchecked")
    public <T extends BasePage> T selectTab(TabName tabName) {
        WebElement tab = tabTemplate.format(tabName.getValue());
        WebElement tabLabel = tabLabelTemplate.format(tabName.getValue());
        WaitUtil.waitForCondition(() -> {
            if (!tab.getAttribute("class").contains("ant-menu-item-selected")) {
                tabLabel.click();
            }
            return tab.getAttribute("class").contains("ant-menu-item-selected");
        }, 10000, 1000, "Waiting for tab '" + tabName.getValue() + "' to become active");

        return switch (tabName) {
            case EDITOR -> (T) new EditorPage();
            case REPOSITORY -> (T) new RepositoryPage();
        };
    }

    @Getter
    public enum TabName {
        EDITOR("Editor"),
        REPOSITORY("Projects");

        private String value;

        TabName(String value) {
            this.value = value;
        }
    }
}