package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

public class EditorTableActionsPanelComponent extends BaseComponent {

    private WebElement saveChangesBtn;
    private WebElement undoChangesBtn;
    private WebElement redoChangesBtn;
    private WebElement insertRowBeforeBtn;
    private WebElement insertRowAfterBtn;
    private WebElement removeRowBtn;
    private WebElement insertColumnBeforeBtn;
    private WebElement removeColumnBtn;

    public EditorTableActionsPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorTableActionsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        saveChangesBtn = createScopedElement("xpath=.//img[@title='Save changes']", "saveChangesBtn");
        undoChangesBtn = createScopedElement("xpath=.//img[@title='Undo changes']", "undoChangesBtn");
        redoChangesBtn = createScopedElement("xpath=.//img[@title='Redo changes']", "redoChangesBtn");
        insertRowBeforeBtn = createScopedElement("xpath=.//img[@id='t_te_insert_row_before']", "insertRowBeforeBtn");
        insertRowAfterBtn = createScopedElement("xpath=.//img[@title='Insert row after']", "insertRowAfterBtn");
        removeRowBtn = createScopedElement("xpath=.//img[@title='Remove row']", "removeRowBtn");
        insertColumnBeforeBtn = createScopedElement("xpath=.//img[@title='Insert column before']", "insertColumnBeforeBtn");
        removeColumnBtn = createScopedElement("xpath=.//img[@title='Remove column']", "removeColumnBtn");
    }

    private void waitWhileTablePanelActionExecuted() {
        WaitUtil.sleep(250, "Waiting for table panel action to complete and UI to update");
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

    public void clickInsertRowBefore() {
        insertRowBeforeBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void clickRemoveRow() {
        removeRowBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void clickInsertColumnBefore() {
        insertColumnBeforeBtn.click();
        waitWhileTablePanelActionExecuted();
    }

    public void clickRemoveColumn() {
        removeColumnBtn.click();
        waitWhileTablePanelActionExecuted();
    }
}
