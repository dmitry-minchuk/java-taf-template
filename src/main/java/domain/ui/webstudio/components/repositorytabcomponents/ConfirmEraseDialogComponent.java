package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class ConfirmEraseDialogComponent extends BaseComponent {

    private WebElement eraseBtn;
    private WebElement cancelBtn;
    private WebElement alsoDeleteFromRepositoryCheckbox;

    public ConfirmEraseDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ConfirmEraseDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        eraseBtn = createScopedElement("xpath=.//form[@id='eraseProjectForm']//input[@value='Erase']", "eraseBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='eraseProjectForm']//input[@value='Cancel']", "cancelBtn");
        alsoDeleteFromRepositoryCheckbox = createScopedElement("xpath=.//table//input[contains(@name, 'eraseProjectForm')]", "alsoDeleteFromRepositoryCheckbox");
    }

    public boolean isAlsoDeleteFromRepositoryVisible() {
        return alsoDeleteFromRepositoryCheckbox.isVisible(1000);
    }

    public void clickErase() {
        eraseBtn.click();
        eraseBtn.waitForHidden(5000);
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
