package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.SaveChangesComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.*;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class EditorPage extends BasePage {

    private static final Logger LOGGER = LogManager.getLogger(EditorPage.class);

    private EditorLeftProjectModuleSelectorComponent editorLeftProjectModuleSelectorComponent;
    private EditorLeftRulesTreeComponent editorLeftRulesTreeComponent;
    private RightTableDetailsComponent rightTableDetailsComponent;
    private TabSwitcherComponent tabSwitcherComponent;
    private TableComponent centerTable;
    private ProblemsPanelComponent problemsPanelComponent;
    private ProjectDetailsComponent projectDetailsComponent;
    private AddModuleComponent addModulePopupComponent;
    private EditorToolbarPanelComponent editorToolbarPanelComponent;
    private EditorTableActionsPanelComponent editorTableActionsPanelComponent;
    private TestResultValidationComponent testResultValidationComponent;
    private EditorMainContentProblemsPanelComponent editorMainContentProblemsPanelComponent;
    private ProjectModuleDetailsComponent projectModuleDetailsComponent;
    private SyncChangesDialogComponent syncChangesDialogComponent;
    private SaveChangesComponent saveChangesComponent;
    private EditProjectDialogComponent editProjectDialogComponent;
    private ExportProjectDialogComponent exportProjectDialogComponent;
    private CopyModuleDialogComponent copyModuleDialogComponent;
    private RemoveModuleDialogComponent removeModulePopupComponent;
    private CreateTableDialogComponent createTableDialogComponent;
    private TopProblemsPanelComponent topProblemsPanelComponent;
    private EditModuleDialogComponent editModuleDialogComponent;
    private WebElement exportProjectBtn;
    private WebElement editProjectIconTemplate;
    private WebElement projectHeaderTemplate;
    private WebElement moduleHeader;
    private WebElement copyModuleBtn;
    private ImportOpenApiDialogComponent importOpenApiDialogComponent;
    private OpenApiModuleSettingsDialogComponent openApiModuleSettingsDialogComponent;
    private WebElement openApiSectionHeader;
    private WebElement importOpenApiImg;
    private WebElement openApiPropertyValueTemplate;
    private ManageDependenciesDialogComponent manageDependenciesDialogComponent;
    private WebElement dependenciesHeader;
    private WebElement addDependenciesLink;
    private WebElement manageDependenciesBtn;
    private WebElement refreshBtn;

    public EditorPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        editorLeftProjectModuleSelectorComponent = createScopedComponent(EditorLeftProjectModuleSelectorComponent.class, "xpath=//div[@id='projects']", "editorLeftProjectModuleSelectorComponent");
        editorLeftRulesTreeComponent = createScopedComponent(EditorLeftRulesTreeComponent.class, "xpath=//div[@id='left']", "editorLeftRulesTreeComponent");
        rightTableDetailsComponent = createScopedComponent(RightTableDetailsComponent.class, "xpath=//div[@id='right']", "rightTableDetailsComponent");
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        centerTable = createScopedComponent(TableComponent.class, "xpath=//table[@class='te_table']", "centerTable");
        editorToolbarPanelComponent = createScopedComponent(EditorToolbarPanelComponent.class, "xpath=//div[@id='tableToolbarPanel']", "editorToolbarPanelComponent");
        testResultValidationComponent = createScopedComponent(TestResultValidationComponent.class, "xpath=//div[@id='content' and contains(@class,'ui-layout-center')]", "testResultValidationComponent");
        problemsPanelComponent = createScopedComponent(ProblemsPanelComponent.class, "xpath=//div[@id='bottom']", "problemsPanelComponent");
        projectDetailsComponent = createScopedComponent(ProjectDetailsComponent.class, "xpath=//div[@class='page']", "projectDetailsComponent");
        addModulePopupComponent = createScopedComponent(AddModuleComponent.class, "xpath=//div[@id='editModulePopup_container']", "addModulePopupComponent");
        editorTableActionsPanelComponent = createScopedComponent(EditorTableActionsPanelComponent.class, "xpath=//div[@class='te_toolbar']", "editorTableActionsPanelComponent");
        editorMainContentProblemsPanelComponent = createScopedComponent(EditorMainContentProblemsPanelComponent.class, "xpath=//div[@id='content']", "editorMainContentProblemsPanelComponent");
        projectModuleDetailsComponent = createScopedComponent(ProjectModuleDetailsComponent.class, "xpath=//div[contains(@class, 'ui-layout-center') and @id='content']", "projectModuleDetailsComponent");
        syncChangesDialogComponent = createScopedComponent(SyncChangesDialogComponent.class, "xpath=//div[@role='dialog' and .//form[@id='merge_branches_form']]", "syncChangesDialogComponent");
        saveChangesComponent = createScopedComponent(SaveChangesComponent.class, "xpath=//div[@id='modalSave_container']", "Save Changes Component");
        editProjectDialogComponent = createScopedComponent(EditProjectDialogComponent.class, "xpath=//div[@id='editProjectPopup_content']", "editProjectDialogComponent");
        exportProjectDialogComponent = createScopedComponent(ExportProjectDialogComponent.class, "xpath=//div[@id='exportProject_container']", "exportProjectDialogComponent");
        copyModuleDialogComponent = createScopedComponent(CopyModuleDialogComponent.class, "xpath=//div[@id='copyModulePopup_container']", "copyModuleDialogComponent");
        removeModulePopupComponent = createScopedComponent(RemoveModuleDialogComponent.class, "xpath=//div[@id='removeModulePopup_content']", "removeModulePopupComponent");
        createTableDialogComponent = createScopedComponent(CreateTableDialogComponent.class, "xpath=//span[@id='wizardData']", "createTableDialogComponent");
        topProblemsPanelComponent = createScopedComponent(TopProblemsPanelComponent.class, "xpath=//div[@id='content']", "topProblemsPanelComponent");
        editModuleDialogComponent = createScopedComponent(EditModuleDialogComponent.class, "xpath=//div[@id='editModulePopup_container']", "editModuleDialogComponent");
        projectHeaderTemplate = new WebElement(getPage(), "xpath=//div[@id='content']//h1[@class='page-header']/span[text()='%s']/..", "projectHeaderTemplate");
        editProjectIconTemplate = new WebElement(getPage(), "xpath=//div[@id='content']//h1[@class='page-header']/span[text()='%s']/..//a[@title='Edit']", "editProjectIconTemplate");
        moduleHeader = new WebElement(getPage(), "xpath=//div[@id='content']//div[@class='page editable']/h1", "moduleHeader");
        copyModuleBtn = new WebElement(getPage(), "xpath=//div[@id='content']//div[@class='page editable']/h1//a[@title='Copy']", "copyModuleBtn");
        importOpenApiDialogComponent = createScopedComponent(ImportOpenApiDialogComponent.class, "xpath=//div[@id='importOpenAPIPopup_container']", "importOpenApiDialogComponent");
        openApiModuleSettingsDialogComponent = createScopedComponent(OpenApiModuleSettingsDialogComponent.class, "xpath=//form[@id='generateOpenAPIForm']", "openApiModuleSettingsDialogComponent");
        openApiSectionHeader = new WebElement(getPage(), "xpath=//div[@class='block editable']//h3[./span[text()='OpenAPI']]", "openApiSectionHeader");
        importOpenApiImg = new WebElement(getPage(), "xpath=//a[@title='Import OpenAPI']/img", "importOpenApiImg");
        openApiPropertyValueTemplate = new WebElement(getPage(), "xpath=//div[@class='block editable']//table[@class='properties']//tr[normalize-space(./td[1]/text())='%s']/td[2]", "openApiPropertyValueTemplate");
        manageDependenciesDialogComponent = createScopedComponent(ManageDependenciesDialogComponent.class, "xpath=//div[@id='manageDependenciesPopup_container']", "manageDependenciesDialogComponent");
        dependenciesHeader = new WebElement(getPage(), "xpath=//div[@id='content']//span[text()='Dependencies']", "dependenciesHeader");
        addDependenciesLink = new WebElement(getPage(), "xpath=//div[@id='content']//a[contains(text(),'Click to add dependencies')]", "addDependenciesLink");
        manageDependenciesBtn = new WebElement(getPage(), "xpath=//div[@id='content']//a[@title='Manage Dependencies']//img", "manageDependenciesBtn");
        refreshBtn = new WebElement(getPage(), "xpath=//a[@id='refreshBtn']", "refreshBtn");
    }

    public EditorToolbarPanelComponent getEditorToolbarPanelComponent() {
        waitUntilSpinnerLoaded();
        return editorToolbarPanelComponent;
    }

    public void navigateToProjectsInBreadcrumbs() {
        getEditorToolbarPanelComponent().navigateToProjectsInBreadcrumbs();
    }

    public TableComponent getCenterTable() {
        WaitUtil.waitForCondition(
                () -> centerTable.isVisible(),
                5000, 250,
                "Waiting for center table to become visible"
        );
        WaitUtil.sleep(500, "Waiting for center table to fully load");
        return centerTable;
    }

    public EditProjectDialogComponent openEditProjectDialog(String projectName) {
        projectHeaderTemplate.format(projectName).hover();
        editProjectIconTemplate.format(projectName).click();
        editProjectDialogComponent.waitForDialogToAppear();
        return editProjectDialogComponent;
    }

    public CopyModuleDialogComponent openCopyModuleDialog() {
        moduleHeader.hover();
        copyModuleBtn.click();
        copyModuleDialogComponent.waitForDialogToAppear();
        return copyModuleDialogComponent;
    }

    public ImportOpenApiDialogComponent openImportOpenApiDialog() {
        openApiSectionHeader.waitForVisible().hover();
        importOpenApiImg.click();
        importOpenApiDialogComponent.waitForVisible();
        return importOpenApiDialogComponent;
    }

    public String getOpenApiPropertyValue(String propertyName) {
        return openApiPropertyValueTemplate.format(propertyName).getText().trim();
    }

    public boolean isOpenApiPropertiesSectionEmpty() {
        if (!openApiSectionHeader.isVisible(2000)) {
            return true;
        }
        WebElement propsTable = new WebElement(page, "xpath=//div[@class='block editable']//table[@class='properties']", "openApiPropsTable");
        if (!propsTable.isVisible(1000)) {
            return true;
        }
        return propsTable.getLocator().locator("xpath=.//tr").count() == 0;
    }

    public void refresh() {
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
    }

    public ManageDependenciesDialogComponent openManageDependenciesDialog() {
        if (addDependenciesLink.isVisible(2000)) {
            addDependenciesLink.click();
        } else {
            dependenciesHeader.hover();
            manageDependenciesBtn.click();
        }
        manageDependenciesDialogComponent.waitForDialogToAppear();
        return manageDependenciesDialogComponent;
    }
}