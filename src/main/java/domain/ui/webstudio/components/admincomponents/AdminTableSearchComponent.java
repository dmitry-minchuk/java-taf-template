package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class AdminTableSearchComponent extends BaseComponent {

    private final WebElement searchInput;

    public AdminTableSearchComponent(String searchTestId) {
        super(DriverPool.getPage());
        searchInput = new WebElement(DriverPool.getPage(),
                "css=[data-testid=" + searchTestId + "] input, input[data-testid=" + searchTestId + "]",
                searchTestId);
    }

    public AdminTableSearchComponent search(String query) {
        searchInput.waitForVisible(DEFAULT_TIMEOUT_MS);
        searchInput.fill(query);
        WaitUtil.sleep(600, "Letting the client-side admin table filter apply");
        return this;
    }

    public AdminTableSearchComponent clear() {
        searchInput.waitForVisible(DEFAULT_TIMEOUT_MS);
        searchInput.fill("");
        WaitUtil.sleep(600, "Letting the admin table filter reset");
        return this;
    }
}
