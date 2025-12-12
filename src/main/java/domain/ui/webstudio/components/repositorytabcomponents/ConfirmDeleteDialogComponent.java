package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import lombok.Getter;

@Getter
public class ConfirmDeleteDialogComponent extends BaseComponent {

    private WebElement deleteBtn;
    private WebElement cancelBtn;

    public ConfirmDeleteDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ConfirmDeleteDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        deleteBtn = createScopedElement("xpath=.//form[@id='deleteNodeForm']//input[@value='Delete']", "deleteBtn");
        cancelBtn = createScopedElement("xpath=.//form[@id='deleteNodeForm']//input[@value='Cancel']", "cancelBtn");
    }
}
