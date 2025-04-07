package domain.ui.webstudio.components;

import configuration.core.SmartWebElement;
import domain.ui.BasePage;
import domain.ui.BasePageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import org.openqa.selenium.support.FindBy;

public class TabSwitcherComponent extends BasePageComponent {

    @FindBy(xpath = "./a[text()='%s']")
    private SmartWebElement tabElement;

    public TabSwitcherComponent() {}

    public BasePage selectTab(TabName tabName) {
        tabElement.format(tabName.getValue()).click();

        return switch (tabName) {
            case EDITOR -> new EditorPage();
            case REPOSITORY -> new RepositoryPage();
            case ADMIN -> new AdminPage();
            default -> throw new IllegalArgumentException("Unknown TabName: " + tabName);
        };
    }

    public static enum TabName {
        EDITOR("EDITOR"),
        REPOSITORY("REPOSITORY"),
        ADMIN("ADMIN");

        private String value;

        TabName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
