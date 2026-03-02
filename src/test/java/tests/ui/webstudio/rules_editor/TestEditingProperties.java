package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEditingProperties extends BaseTest {

    private static final String PROJECT_NAME = "TestAddDeleteEditProperties";
    private static final String EXCEL_FILE = "TestAddDeleteEditProperties.xlsx";

    @Test
    @TestCaseId("IPBQA-25861")
    @Description("Rules Editor - Edit existing properties in table details")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEditingProperties() {
        EditorPage editorPage = loginAndCreateProject();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, PROJECT_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MyRules1");

        editAndCheckProperty(editorPage, "Category", "category", "cat2");
        editAndCheckProperty(editorPage, "Description", "description", "Hello Kitty2");
        editAndCheckProperty(editorPage, "Tags", "tags", "Tag3,Tag4");
        editAndCheckProperty(editorPage, "Effective Date", "effectiveDate", "05/14/2018");
        editAndCheckProperty(editorPage, "Expiration Date", "expirationDate", "05/16/2018");
        editAndCheckProperty(editorPage, "Start Request Date", "startRequestDate", "05/14/2017");
        editAndCheckProperty(editorPage, "End Request Date", "endRequestDate", "04/13/2016");
        editAndCheckProperty(editorPage, "LOB", "lob", "007");
        editAndCheckProperty(editorPage, "Nature", "nature", "TestNature1");
        editAndCheckProperty(editorPage, "ID", "id", "test2");
        editAndCheckProperty(editorPage, "Build Phase", "buildPhase", "Property2");

        editAndCheckCheckboxProperty(editorPage, "Canada Region", "caRegions", "QC");
        editAndCheckCheckboxProperty(editorPage, "Canada Province", "caProvinces", "NT", "YT");
        editAndCheckCheckboxProperty(editorPage, "Countries", "country", "BY");
        editAndCheckCheckboxProperty(editorPage, "Currency", "currency", "YER");
        editAndCheckCheckboxProperty(editorPage, "Language", "lang", "SPA");
        editAndCheckCheckboxProperty(editorPage, "US Region", "usregion", "NE");
        editAndCheckCheckboxProperty(editorPage, "US States", "state", "WA", "WV");

        editAndCheckBooleanProperty(editorPage, "Cacheable", "cacheable", false);

        editAndCheckDropdownProperty(editorPage, "Origin", "origin", "Deviation");
        editAndCheckDropdownProperty(editorPage, "Recalculate", "recalculate", "Analyze");
        editAndCheckDropdownProperty(editorPage, "Validate DT", "validateDT", "Off");
        editAndCheckDropdownProperty(editorPage, "Empty Result Processing", "emptyResultProcessing", "Skip");
    }

    private EditorPage loginAndCreateProject() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, PROJECT_NAME, EXCEL_FILE);

        return repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
    }

    private void editAndCheckProperty(EditorPage editorPage, String propertyName, String propertyTableName, String newValue) {
        WaitUtil.sleep(300, "Waiting before editing property");
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();

        if (propertyName.contains("Date")) {
            tableDetails.editDateProperty(propertyName, newValue);
            tableDetails.clickSaveBtn();
            newValue = formatDate(newValue);
        } else {
            tableDetails.editTextProperty(propertyName, newValue);
            tableDetails.clickSaveBtn();
        }

        assertThat(editorPage.getCenterTable().getPropertyValue(propertyTableName))
                .as("Property '%s' should have value '%s'", propertyTableName, newValue)
                .isEqualTo(newValue);
    }

    private void editAndCheckCheckboxProperty(EditorPage editorPage, String propertyName, String propertyTableName, String... values) {
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();
        tableDetails.editCheckboxProperty(propertyName, values);
        tableDetails.clickSaveBtn();

        String expectedValue = String.join(",", values);
        assertThat(editorPage.getCenterTable().getPropertyValue(propertyTableName))
                .as("Property '%s' should have value '%s'", propertyTableName, expectedValue)
                .isEqualTo(expectedValue);
    }

    private void editAndCheckBooleanProperty(EditorPage editorPage, String propertyName, String propertyTableName, boolean value) {
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();
        tableDetails.editBooleanProperty(propertyName, value);
        tableDetails.clickSaveBtn();

        assertThat(editorPage.getCenterTable().getPropertyValue(propertyTableName))
                .as("Property '%s' should have value '%s'", propertyTableName, value)
                .isEqualTo(String.valueOf(value));
    }

    private void editAndCheckDropdownProperty(EditorPage editorPage, String propertyName, String propertyTableName, String value) {
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();
        tableDetails.editDropdownProperty(propertyName, value);
        tableDetails.clickSaveBtn();

        assertThat(editorPage.getCenterTable().getPropertyValue(propertyTableName))
                .as("Property '%s' should have value '%s'", propertyTableName, value)
                .isEqualToIgnoringCase(value);
    }

    private String formatDate(String dateValue) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("M/d/yy");
        try {
            Date date = inputFormat.parse(dateValue);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateValue;
        }
    }
}
