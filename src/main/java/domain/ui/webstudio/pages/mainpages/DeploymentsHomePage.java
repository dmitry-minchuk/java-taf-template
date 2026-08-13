package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;

public class DeploymentsHomePage extends BasePage {

    private WebElement homeRoot;
    private WebElement noRepositoriesPlaceholder;

    public DeploymentsHomePage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        homeRoot = new WebElement(page, "[data-testid=deployments-home]", "deploymentsHome");
        noRepositoriesPlaceholder = new WebElement(page,
                "[data-testid=deployments-empty-repositories]", "deploymentsEmptyRepositories");
    }

    public DeploymentsHomePage open() {
        java.net.URI current = java.net.URI.create(page.url());
        page.navigate(current.getScheme() + "://" + current.getAuthority() + "/deployments");
        homeRoot.waitForVisible(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
        return this;
    }

    public boolean isHomeShown(int timeoutInMillis) {
        return homeRoot.isVisible(timeoutInMillis);
    }

    public boolean isNoRepositoriesShown(int timeoutInMillis) {
        return noRepositoriesPlaceholder.isVisible(timeoutInMillis);
    }
}
