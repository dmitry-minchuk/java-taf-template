package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import domain.ui.webstudio.pages.mainpages.PlaywrightRepositoryPage;
import lombok.Getter;

public class PlaywrightTabSwitcherComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement tabTemplate;

    public PlaywrightTabSwitcherComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTabSwitcherComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tabTemplate = createScopedElement("xpath=./li[./span[text()='%s']]", "selectedTab");
    }

    @SuppressWarnings("unchecked")
    public <T extends PlaywrightBasePage> T selectTab(TabName tabName) {
        tabTemplate.format(tabName.getValue()).click();

        return switch (tabName) {
            case EDITOR -> (T) new PlaywrightEditorPage();
            case REPOSITORY -> (T) new PlaywrightRepositoryPage();
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