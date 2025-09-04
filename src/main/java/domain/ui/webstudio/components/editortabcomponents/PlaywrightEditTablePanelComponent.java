package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;

public class PlaywrightEditTablePanelComponent extends CoreComponent {

    private PlaywrightWebElement saveChangesBtn;
    private PlaywrightWebElement undoChangesBtn;
    private PlaywrightWebElement redoChangesBtn;
    private PlaywrightWebElement insertRowAfterBtn;
    private PlaywrightWebElement removeRowBtn;

    public PlaywrightEditTablePanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightEditTablePanelComponent(PlaywrightWebElement rootLocator) {
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