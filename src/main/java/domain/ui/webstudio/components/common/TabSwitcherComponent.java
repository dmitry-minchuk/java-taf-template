package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.BasePage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.WaitUtil;
import lombok.Getter;

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
        // The <li> carries the state class, so it is what "is this tab active?" is read from.
        tabTemplate = createScopedElement("xpath=./li[./span[text()='%s']]", "selectedTab");
        // ...but the click goes to the label inside it. The <li> is 48.5px tall in a 49px header and sits at
        // y=-1.3, i.e. its top pixel is above the viewport, and the page has nothing to scroll (scrollHeight
        // == clientHeight). Playwright's scrollIntoViewIfNeeded therefore keeps trying to bring the <li>
        // fully into view, never succeeds, and the click times out with "element is not stable". The label
        // sits at y=14.8 and is fully inside the viewport, so clicking it lands every time.
        tabLabelTemplate = createScopedElement("xpath=./li[./span[text()='%s']]/span", "selectedTabLabel");
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
        // Renamed from "Repository" to "Projects" in the React nav (build 032c60a664ce+).
        REPOSITORY("Projects");

        private String value;

        TabName(String value) {
            this.value = value;
        }
    }
}