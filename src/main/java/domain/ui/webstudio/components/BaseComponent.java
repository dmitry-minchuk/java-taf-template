package domain.ui.webstudio.components;

import com.microsoft.playwright.Page;
import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;

// This class is separated from CoreComponent and created for specific element storage
public abstract class BaseComponent extends CoreComponent {

    private WebElement contentLoadingSpinner;

    public BaseComponent(Page page) {
        super(page);
        initializeElements();
    }

    public BaseComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        contentLoadingSpinner = new WebElement(page, "//div[@id='loadingPanel']", "contentLoadingSpinner");
    }

    public void waitUntilSpinnerLoaded() {
        contentLoadingSpinner.waitForHidden(30000);
    }
}