package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.components.createnewproject.PlaywrightExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightZipArchiveComponent;

/**
 * Playwright version of CreateNewProjectComponent for project creation modal with tab switching
 * Supports EXCEL_FILES, ZIP_ARCHIVE, and other project types with Playwright-native components
 */
public class PlaywrightCreateNewProjectComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement tabElement;
    private PlaywrightExcelFilesComponent excelFilesComponent;
    private PlaywrightZipArchiveComponent zipArchiveComponent;

    public PlaywrightCreateNewProjectComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightCreateNewProjectComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Tab selector: "xpath=.//span[@class='rf-tab-lbl' and contains(text(), '%s')]"
        tabElement = createScopedElement("xpath=.//span[@class='rf-tab-lbl' and contains(text(), '%s')]", "tabElement");
        
        // Initialize sub-components with scoped locators
        excelFilesComponent = createScopedComponent(PlaywrightExcelFilesComponent.class, 
            "xpath=.//form[@name='createProjectFormFiles']", "excelFilesComponent");
        zipArchiveComponent = createScopedComponent(PlaywrightZipArchiveComponent.class, 
            "xpath=.//form[@name='uploadProjectForm']", "zipArchiveComponent");
    }

    @SuppressWarnings("unchecked")
    public <T extends PlaywrightBasePageComponent> T selectTab(CreateNewProjectComponent.TabName tabName) {
        String selector = String.format("xpath=.//span[@class='rf-tab-lbl' and contains(text(), '%s')]", tabName.getValue());
        PlaywrightWebElement projectTabLabel = createScopedElement(selector, "projectTabLabel");
        projectTabLabel.click();

        return switch (tabName) {
            case EXCEL_FILES -> (T) excelFilesComponent;
            case ZIP_ARCHIVE -> (T) zipArchiveComponent;
            default -> throw new IllegalArgumentException("Unsupported tab type: " + tabName);
        };
    }
}