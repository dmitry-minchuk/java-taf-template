package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.PlaywrightTableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightAddDeleteRowWithoutSaving extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7474")
    @Description("BUG: Added row is deleted incorrectly - the value of the next row is changed")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddDeleteRowWithoutSaving() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectOpenEditor(User.ADMIN, TEMPLATE, "Tutorial 6 - Introduction to Spreadsheet Tables");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "Tutorial6 - Intro to Spreadsheet Tables");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "LossFreeDiscount");

        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        PlaywrightTableComponent table = editorPage.getCenterTable();
        table.clickCell(4,1);
        editorPage.getEditTablePanelComponent().getInsertRowAfterBtn().click();
        table.doubleClickAndPasteTextToCell(5, 0, "444", false);
        editorPage.getEditTablePanelComponent().getRemoveRowBtn().click();
        assertThat(StringUtils.normalizeSpace(table.getCellText(5, 0))).isEmpty();
        assertThat(table.getCellText(5, 1)).isEqualTo("0%");
        editorPage.getEditTablePanelComponent().getSaveChangesBtn().click();
        assertThat(StringUtils.normalizeSpace(table.getCellText(5, 0))).isEmpty();
        assertThat(table.getCellText(5, 1)).isEqualTo("0%");
    }
}
