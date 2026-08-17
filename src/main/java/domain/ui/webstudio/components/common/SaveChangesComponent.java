package domain.ui.webstudio.components.common;

import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SaveProjectDialogComponent;
import lombok.Getter;

@Getter
public class SaveChangesComponent extends BaseComponent {

    private final SaveProjectDialogComponent saveProjectDialog;

    public SaveChangesComponent() {
        super(DriverPool.getPage());
        saveProjectDialog = new SaveProjectDialogComponent();
    }

    public void clickSave() {
        saveProjectDialog.waitForVisible();
        try {
            saveProjectDialog.submit();
        } catch (RuntimeException shadeInTheWay) {
            saveProjectDialog.submitThroughShade();
        }
    }
}
