package domain.ui.webstudio.components.common;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.BasePage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import lombok.Getter;

public class TabSwitcherComponent extends CoreComponent {

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
        tabTemplate.format(tabName.getValue()).click();

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