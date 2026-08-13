package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDatatypeRoundTripUi extends BaseTest {

    private static final String PROJECT = "DatatypeRoundTrip";
    private static final String FIXTURE_XLSX = "TitledDatatype.xlsx";
    private static final String MODULE = "TitledDatatype";
    private static final String DATATYPE_FOLDER = "Datatype";
    private static final String REORDERED_DATATYPE = "Person";
    private static final int TITLE_ROW = 2;
    private static final int FIRST_FIELD_ROW = 3;
    private static final int LINKED_TYPE_ROW = 4;
    private static final int NAME_COLUMN = 1;
    private static final int TYPE_COLUMN = 2;
    private static final String RENAMED_FIELD = "renamedName";

    @Test
    @TestCaseId("EPBDS-16428")
    @Description("EPBDS-16428 and EPBDS-16426: a titled datatype whose columns are REORDERED (Name before Type) "
            + "must survive an editor round-trip - the title row stays a title, the first field stays a field, "
            + "the edited field name persists, and the Type cell keeps its link to the datatype it names.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testReorderedTitledDatatypeSurvivesEditorRoundTrip() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, PROJECT, FIXTURE_XLSX);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, MODULE);
        openDatatype(editorPage);

        TableComponent table = editorPage.getCenterTable();
        assertThat(table.getCellText(TITLE_ROW, NAME_COLUMN))
                .as("Precondition: the reordered datatype must title its first column Name")
                .isEqualTo("Name");
        assertThat(table.getCellText(TITLE_ROW, TYPE_COLUMN))
                .as("Precondition: the reordered datatype must title its second column Type")
                .isEqualTo("Type");
        assertThat(isTypeCellLinked(table))
                .as("Precondition: the Address type cell must link to the datatype it names")
                .isTrue();

        table.editCell(FIRST_FIELD_ROW, NAME_COLUMN, RENAMED_FIELD);
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, MODULE);
        openDatatype(editorPage);

        TableComponent reopened = editorPage.getCenterTable();
        assertThat(reopened.getCellText(TITLE_ROW, NAME_COLUMN))
                .as("The title row must stay a title after the round-trip, not become a field (EPBDS-16428)")
                .isEqualTo("Name");
        assertThat(reopened.getCellText(FIRST_FIELD_ROW, NAME_COLUMN))
                .as("The edited first field must hold the new name after the round-trip")
                .isEqualTo(RENAMED_FIELD);
        assertThat(reopened.getCellText(LINKED_TYPE_ROW, TYPE_COLUMN))
                .as("The second field must keep its declared datatype after the round-trip")
                .isEqualTo("Address");
        assertThat(isTypeCellLinked(reopened))
                .as("The Type cell must keep its link to the datatype it names (EPBDS-16428)")
                .isTrue();
    }

    private void openDatatype(EditorPage editorPage) {
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(DATATYPE_FOLDER)
                .selectItemInFolder(DATATYPE_FOLDER, REORDERED_DATATYPE);
        editorPage.waitUntilSpinnerLoaded();
    }

    private boolean isTypeCellLinked(TableComponent table) {
        return typeCellLinkText(table).equals("Address");
    }

    private String typeCellLinkText(TableComponent table) {
        var link = table.getRow(LINKED_TYPE_ROW).getCells().get(TYPE_COLUMN - 1)
                .getLocator().locator("xpath=.//a");
        return link.count() == 0 ? "" : link.first().innerText().trim();
    }
}
