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

@Getter
public class EditorPage extends BasePage {

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
    private WebElement exportProjectBtn;
    private WebElement editProjectIconTemplate;
    private WebElement projectHeaderTemplate;
    private WebElement moduleHeader;
    private WebElement copyModuleBtn;

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
        projectHeaderTemplate = new WebElement(getPage(), "xpath=//div[@id='content']//h1[@class='page-header']/span[text()='%s']/..", "projectHeaderTemplate");
        editProjectIconTemplate = new WebElement(getPage(), "xpath=//div[@id='content']//h1[@class='page-header']/span[text()='%s']/..//img", "editProjectIconTemplate");
        moduleHeader = new WebElement(getPage(), "xpath=//div[@id='content']//div[@class='page editable']/h1", "moduleHeader");
        copyModuleBtn = new WebElement(getPage(), "xpath=//div[@id='content']//div[@class='page editable']/h1//a[@title='Copy']", "copyModuleBtn");
    }

    public EditorToolbarPanelComponent getEditorToolbarPanelComponent() {
        waitUntilSpinnerLoaded();
        return editorToolbarPanelComponent;
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
}