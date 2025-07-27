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
        saveChangesBtn = new PlaywrightWebElement(page, "./img[@title='Save changes']", "Save Changes Button");
        undoChangesBtn = new PlaywrightWebElement(page, "./img[@title='Undo changes']", "Undo Changes Button");
        redoChangesBtn = new PlaywrightWebElement(page, "./img[@title='Redo changes']", "Redo Changes Button");
        insertRowAfterBtn = new PlaywrightWebElement(page, "./img[@title='Insert row after']", "Insert Row After Button");
        removeRowBtn = new PlaywrightWebElement(page, "./img[@title='Remove row']", "Remove Row Button");
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