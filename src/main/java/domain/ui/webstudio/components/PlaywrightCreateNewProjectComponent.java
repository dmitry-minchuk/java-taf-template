package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.components.createnewproject.PlaywrightExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightZipArchiveComponent;

public class PlaywrightCreateNewProjectComponent extends PlaywrightBasePageComponent {

    private PlaywrightExcelFilesComponent excelFilesComponent;
    private PlaywrightZipArchiveComponent zipArchiveComponent;

    private PlaywrightWebElement tabTemplate;

    public PlaywrightCreateNewProjectComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightCreateNewProjectComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tabTemplate = createScopedElement("xpath=.//span[@class='rf-tab-lbl' and contains(text(), '%s')]", "projectTabLabel");
        excelFilesComponent = createScopedComponent(PlaywrightExcelFilesComponent.class, "xpath=.//form[@name='createProjectFormFiles']", "excelFilesComponent");
        zipArchiveComponent = createScopedComponent(PlaywrightZipArchiveComponent.class, "xpath=.//form[@name='uploadProjectForm']", "zipArchiveComponent");
    }

    @SuppressWarnings("unchecked")
    public <T extends PlaywrightBasePageComponent> T selectTab(CreateNewProjectComponent.TabName tabName) {
        tabTemplate.format(tabName.getValue()).click();

        return switch (tabName) {
            case EXCEL_FILES -> (T) excelFilesComponent;
            case ZIP_ARCHIVE -> (T) zipArchiveComponent;
            default -> throw new IllegalArgumentException("Unsupported tab type: " + tabName);
        };
    }
}