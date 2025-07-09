package domain.ui.webstudio.components;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePage;
import configuration.core.ui.BasePageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class TabSwitcherComponent extends BasePageComponent {

    @FindBy(xpath = "./li[./span[text()='%s']]")
    private SmartWebElement tabElement;

    public TabSwitcherComponent() {}

    @SuppressWarnings("unchecked")
    public <T extends BasePage> T selectTab(TabName tabName) {
        tabElement.format(tabName.getValue()).click();

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
