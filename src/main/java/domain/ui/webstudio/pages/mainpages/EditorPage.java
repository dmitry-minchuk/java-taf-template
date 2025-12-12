package domain.ui.webstudio.pages.mainpages;

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
        syncChangesDialogComponent = createScopedComponent(SyncChangesDialogComponent.class, "xpath=//div[@id='modalMergeBranches_container']", "syncChangesDialogComponent");
        saveChangesComponent = createScopedComponent(SaveChangesComponent.class, "xpath=//div[@id='modalSave_container']", "Save Changes Component");
    }

    public EditorToolbarPanelComponent getEditorToolbarPanelComponent() {
        waitUntilSpinnerLoaded();
        return editorToolbarPanelComponent;
    }

    public TableComponent getCenterTable() {
        centerTable.isVisible();
        WaitUtil.sleep(500, "Waiting for center table to fully load");
        return centerTable;
    }
}