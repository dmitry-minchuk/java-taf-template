package domain.ui.webstudio.components;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import domain.ui.webstudio.components.createnewproject.*;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class CreateNewProjectComponent extends BasePageComponent {

    @FindBy(xpath = ".//span[@class='rf-tab-lbl' and contains(text(), '%s')]")
    private SmartWebElement tabElement;

    @FindBy(xpath = ".//form[@name='createProjectFormTempl']")
    private TemplateTabComponent templateTabComponent;

    @FindBy(xpath = ".//form[@name='createProjectFormFiles']")
    private ExcelFilesComponent excelFilesComponent;

    @FindBy(xpath = ".//form[@name='uploadProjectForm']")
    private ZipArchiveComponent zipArchiveComponent;

    @FindBy(xpath = ".//form[@name='openAPIProjectForm']")
    private OpenApiComponent openApiComponent;

    @FindBy(xpath = ".//form[@name='uploadWorkspaceProjectForm']")
    private WorkspaceComponent workspaceComponent;

    public CreateNewProjectComponent() {
    }

    @SuppressWarnings("unchecked")
    public <T extends BasePageComponent> T selectTab(TabName tabName) {
        tabElement.format(tabName.getValue()).click();

        return switch (tabName) {
            case TEMPLATE -> (T) templateTabComponent;
            case EXCEL_FILES -> (T) excelFilesComponent;
            case ZIP_ARCHIVE -> (T) zipArchiveComponent;
            case OPEN_API -> (T) openApiComponent;
            case WORKSPACE -> (T) workspaceComponent;
        };
    }

    @Getter
    public enum TabName {
        TEMPLATE("Template"),
        EXCEL_FILES("Excel Files"),
        ZIP_ARCHIVE("Zip Archive"),
        OPEN_API("OpenAPI"),
        WORKSPACE("Workspace");

        private String value;

        TabName(String value) {
            this.value = value;
        }
    }
}
