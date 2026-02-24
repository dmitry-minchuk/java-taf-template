package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.BasePage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.WaitUtil;
import lombok.Getter;

public class TabSwitcherComponent extends BaseComponent {

    private WebElement tabTemplate;

    public TabSwitcherComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TabSwitcherComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tabTemplate = createScopedElement("xpath=./li[./span[text()='%s']]", "selectedTab");
    }

    @SuppressWarnings("unchecked")
    public <T extends BasePage> T selectTab(TabName tabName) {
        WebElement tab = tabTemplate.format(tabName.getValue());
        WaitUtil.waitForCondition(() -> {
            if (!tab.getAttribute("class").contains("ant-menu-item-selected")) {
                tab.click();
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
        REPOSITORY("Repository");

        private String value;

        TabName(String value) {
            this.value = value;
        }
    }
}