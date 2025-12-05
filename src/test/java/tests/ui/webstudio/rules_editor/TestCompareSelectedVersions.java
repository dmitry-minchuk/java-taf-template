package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompareSelectedVersions extends BaseTest {

    @Test
    @TestCaseId("Test030")
    @Description("Check compare 3 selected versions logic - max 2 versions can be selected")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompareSelectedVersions() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestCompareSelectedVersions.rules.xls");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "TestCompareSelectedVersions.rules");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        TableComponent table = editorPage.getCenterTable();
        table.editCell(8, 2, "5");
        editorPage.getEditTablePanelComponent().clickSaveChanges();
        table.editCell(9, 2, "15");
        editorPage.getEditTablePanelComponent().clickSaveChanges();
        table.editCell(10, 2, "25");
        editorPage.getEditTablePanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getTableToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(2, true);
        changesDialog.setCompareCheckbox(3, true);
        changesDialog.setCompareCheckbox(4, true);

        assertThat(changesDialog.getCompareCheckboxValue(2)).isFalse();
        assertThat(changesDialog.getCompareCheckboxValue(3)).isTrue();
        assertThat(changesDialog.getCompareCheckboxValue(4)).isTrue();
    }
}
