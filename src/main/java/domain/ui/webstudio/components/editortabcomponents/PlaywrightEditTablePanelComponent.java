package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;

@Getter
public class PlaywrightEditTablePanelComponent extends PlaywrightBasePageComponent {

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
        saveChangesBtn = createScopedElement("xpath=./img[@title='Save changes']", "saveChangesBtn");
        undoChangesBtn = createScopedElement("xpath=./img[@title='Undo changes']", "undoChangesBtn");
        redoChangesBtn = createScopedElement("xpath=./img[@title='Redo changes']", "redoChangesBtn");
        insertRowAfterBtn = createScopedElement("xpath=./img[@title='Insert row after']", "insertRowAfterBtn");
        removeRowBtn = createScopedElement("xpath=./img[@title='Remove row']", "removeRowBtn");
    }

    public void saveChanges() {
        saveChangesBtn.click();
    }

    public void undoChanges() {
        undoChangesBtn.click();
    }

    public void redoChanges() {
        redoChangesBtn.click();
    }

    public void insertRowAfter() {
        insertRowAfterBtn.click();
    }

    public void removeRow() {
        removeRowBtn.click();
    }

    public boolean isSaveChangesEnabled() {
        return saveChangesBtn.isEnabled();
    }

    public boolean isUndoChangesEnabled() {
        return undoChangesBtn.isEnabled();
    }

    public boolean isRedoChangesEnabled() {
        return redoChangesBtn.isEnabled();
    }
}