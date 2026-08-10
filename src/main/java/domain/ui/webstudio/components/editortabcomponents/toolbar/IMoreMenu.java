package domain.ui.webstudio.components.editortabcomponents.toolbar;

import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareExcelFilesDialogComponent;

/** The More dropdown of the top toolbar. */
public interface IMoreMenu {
    ChangesDialogComponent clickChanges();
    void clickRevisions();
    CompareExcelFilesDialogComponent clickCompareExcelFiles();
    void clickTableDependencies();
}
