package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.ui.webstudio.components.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import tests.BaseTest;


import static org.assertj.core.api.Assertions.assertThat;

public class TestAddDeleteRowWithoutSaving extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7474")
    @Description("BUG: Added row is deleted incorrectly - the value of the next row is changed")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAddDeleteRowWithoutSaving() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Tutorial 6 - Introduction to Spreadsheet Tables");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Tutorial6 - Intro to Spreadsheet Tables");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "LossFreeDiscount");

        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        TableComponent table = editorPage.getCenterTable();
        table.clickCell(5, 2);
        editorPage.getEditTablePanelComponent().clickInsertRowAfter();
        table.editCell(6, 1, "444", false);
        editorPage.getEditTablePanelComponent().clickRemoveRow();
        assertThat(StringUtils.normalizeSpace(table.getCellText(6, 1))).isEmpty();
        assertThat(table.getCellText(6, 2)).isEqualTo("0%");
        editorPage.getEditTablePanelComponent().clickSaveChanges();
        assertThat(StringUtils.normalizeSpace(table.getCellText(6, 1))).isEmpty();
        assertThat(table.getCellText(6, 2)).isEqualTo("0%");
    }
}
