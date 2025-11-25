package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import lombok.Getter;

@Getter
public class SaveChangesComponent extends BaseComponent {

    private WebElement commentField;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    public SaveChangesComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SaveChangesComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        commentField = createScopedElement("xpath=.//textarea[@id='saveForm:comment']", "commentField");
        saveBtn = createScopedElement("xpath=.//input[@value='Save']", "Save Button");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "Cancel Button");
    }
}
