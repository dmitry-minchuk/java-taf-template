package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import domain.ui.webstudio.pages.mainpages.PlaywrightRepositoryPage;
import lombok.Getter;

/**
 * Playwright version of TabSwitcherComponent for navigating between EDITOR and REPOSITORY tabs
 * Uses native Playwright selectors and wait mechanisms
 */
public class PlaywrightTabSwitcherComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement tabElement;

    public PlaywrightTabSwitcherComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTabSwitcherComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Scoped to this component: "xpath=.//li[.//span[text()='%s']]"
        tabElement = createScopedElement("xpath=.//li[.//span[text()='%s']]", "tabElement");
    }

    @SuppressWarnings("unchecked")
    public <T extends PlaywrightBasePage> T selectTab(TabName tabName) {
        tabElement.format(tabName.getValue()).click();

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