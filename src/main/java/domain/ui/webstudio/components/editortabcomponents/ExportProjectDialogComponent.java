package domain.ui.webstudio.components.editortabcomponents;

import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ExportProjectModalComponent;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class ExportProjectDialogComponent extends BaseComponent {

    private final ExportProjectModalComponent exportProjectModal;

    public ExportProjectDialogComponent() {
        super(DriverPool.getPage());
        exportProjectModal = new ExportProjectModalComponent();
    }

    public void waitForDialogToAppear() {
        exportProjectModal.waitForDialogToAppear();
    }

    public boolean isDialogVisible() {
        return exportProjectModal.isDialogVisible();
    }

    public List<String> getAllRevisions() {
        return exportProjectModal.getAllRevisions();
    }

    public String getSelectedRevision() {
        return exportProjectModal.getSelectedRevision();
    }

    public void selectRevision(String revision) {
        exportProjectModal.selectRevision(revision);
    }

    public void clickExport() {
        exportProjectModal.clickExport();
    }

    public File clickExportAndDownload() {
        return exportProjectModal.clickExportAndDownload();
    }

    public void clickCancel() {
        exportProjectModal.clickCancel();
    }
}
