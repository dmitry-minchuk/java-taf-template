package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

public class EditTablePanelComponent extends BaseComponent {

    private WebElement saveChangesBtn;
    private WebElement undoChangesBtn;
    private WebElement redoChangesBtn;
    private WebElement insertRowAfterBtn;
    private WebElement removeRowBtn;

    public EditTablePanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditTablePanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        saveChangesBtn = createScopedElement("xpath=.//img[@title='Save changes']", "saveChangesBtn");
        undoChangesBtn = createScopedElement("xpath=.//img[@title='Undo changes']", "undoChangesBtn");
        redoChangesBtn = createScopedElement("xpath=.//img[@title='Redo changes']", "redoChangesBtn");
        insertRowAfterBtn = createScopedElement("xpath=.//img[@title='Insert row after']", "insertRowAfterBtn");
        removeRowBtn = createScopedElement("xpath=.//img[@title='Remove row']", "removeRowBtn");
    }

    private void waitWhileTablePanelActionExecuted() {
        WaitUtil.sleep(250);
    }

    public void clickSaveChanges() {
        saveChangesBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void undoClickChanges() {
        undoChangesBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void redoClickChanges() {
        redoChangesBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void clickInsertRowAfter() {
        insertRowAfterBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void clickRemoveRow() {
        removeRowBtn.click();
        waitWhileTablePanelActionExecuted();
    }
}