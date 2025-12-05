package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestModuleCategoryInheritedProperties extends BaseTest {

    private final Map<String, String> valuesModuleProperties = Map.ofEntries(
        Map.entry("LOB", "001"),
        Map.entry("US Region", "MW"),
        Map.entry("Countries", "BY"),
        Map.entry("Start Request Date", "01/01/2016"),
        Map.entry("End Request Date", "01/02/2016"),
        Map.entry("Expiration Date", "01/03/2016"),
        Map.entry("Effective Date", "01/04/2016"),
        Map.entry("Currency", "SYP"),
        Map.entry("Language", "UKR"),
        Map.entry("US States", "NV"),
        Map.entry("Origin", "Base"),
        Map.entry("Canada Province", "MB"),
        Map.entry("Canada Region", "QC"),
        Map.entry("Build Phase", "ModuleProperty"),
        Map.entry("Validate DT", "ON"),
        Map.entry("Fail On Miss", "true"),
        Map.entry("Empty Result Processing", "RETURN")
    );

    private final Map<String, String> valuesModuleOverwrittenByTableProperties = Map.ofEntries(
        Map.entry("LOB", "002"),
        Map.entry("US Region", "SE"),
        Map.entry("Countries", "GB"),
        Map.entry("Start Request Date", "02/01/2016"),
        Map.entry("End Request Date", "02/02/2016"),
        Map.entry("Expiration Date", "02/03/2016"),
        Map.entry("Effective Date", "02/04/2016"),
        Map.entry("Currency", "GBP"),
        Map.entry("Language", "DUT"),
        Map.entry("US States", "TX"),
        Map.entry("Origin", "Deviation"),
        Map.entry("Canada Province", "NL"),
        Map.entry("Canada Region", "QC,HQ"),
        Map.entry("Build Phase", "ModuleOverlapped"),
        Map.entry("Validate DT", "OFF"),
        Map.entry("Fail On Miss", "false"),
        Map.entry("Empty Result Processing", "SKIP")
    );

    private final Map<String, String> valuesCategoryProperties = Map.ofEntries(
        Map.entry("LOB", "003"),
        Map.entry("US Region", "SW"),
        Map.entry("Countries", "PT"),
        Map.entry("Start Request Date", "03/01/2016"),
        Map.entry("End Request Date", "03/02/2016"),
        Map.entry("Expiration Date", "03/03/2016"),
        Map.entry("Effective Date", "03/04/2016"),
        Map.entry("Currency", "USD"),
        Map.entry("Language", "GER"),
        Map.entry("US States", "FL,PR,TX"),
        Map.entry("Origin", "Base"),
        Map.entry("Canada Province", "NT"),
        Map.entry("Canada Region", "HQ"),
        Map.entry("Build Phase", "BuildCategoryPhase"),
        Map.entry("Validate DT", "ON"),
        Map.entry("Fail On Miss", "false"),
        Map.entry("Empty Result Processing", "SKIP")
    );

    private final Map<String, String> valuesCategoryOverwrittenByTableProperties = Map.ofEntries(
        Map.entry("LOB", "004"),
        Map.entry("US Region", "MW,NE,SW"),
        Map.entry("Countries", "ZA"),
        Map.entry("Start Request Date", "04/01/2016"),
        Map.entry("End Request Date", "04/02/2016"),
        Map.entry("Expiration Date", "04/03/2016"),
        Map.entry("Effective Date", "04/04/2016"),
        Map.entry("Currency", "VEF"),
        Map.entry("Language", "GER,THA,TUR,UKR"),
        Map.entry("US States", "WI"),
        Map.entry("Origin", "Deviation"),
        Map.entry("Canada Province", "PE"),
        Map.entry("Canada Region", "QC"),
        Map.entry("Build Phase", "BuildCategoryOverwrite"),
        Map.entry("Validate DT", "OFF"),
        Map.entry("Fail On Miss", "true"),
        Map.entry("Empty Result Processing", "RETURN")
    );

    @Test
    @TestCaseId("IPBQA-25884")
    @Description("Module and Category inherited properties: verify property inheritance from module/category, overwritten values, and blue arrow navigation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testModuleAndCategoryInheritedProperties() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestModuleCategoryInheritedProperties.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestModuleCategoryInheritedProperties");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MyRules1");
        verifyPropertiesInTableDetails(editorPage, valuesModuleProperties);

        verifyBlueArrowWork(editorPage);

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MyRules7");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Auto Type Discovery"))
                .contains("true");

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "MyRules2Test");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Precision"))
                .contains("1");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Decision", "MyRules2");
        verifyPropertiesInTableDetails(editorPage, valuesModuleOverwrittenByTableProperties);

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", "MyRules1Test");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Precision"))
                .contains("2");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Decision", "MyRules3");
        verifyPropertiesInTableDetails(editorPage, valuesCategoryProperties);

        verifyBlueArrowWork(editorPage);

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Spreadsheet", "MyRules9");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Auto Type Discovery"))
                .contains("false");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", "MyRules4Test");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Precision"))
                .contains("3");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Decision", "MyRules4");
        verifyPropertiesInTableDetails(editorPage, valuesCategoryOverwrittenByTableProperties);

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Spreadsheet", "MyRules8");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Auto Type Discovery"))
                .contains("true");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", "MyRules3Test");
        assertThat(editorPage.getRightTableDetailsComponent().getPropertyValue("Precision"))
                .contains("4");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Decision", "MyRules5");
        verifyPropertiesInTableDetails(editorPage, valuesCategoryProperties);
    }

    private void verifyPropertiesInTableDetails(EditorPage editorPage, Map<String, String> expectedValues) {
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();
        expectedValues.forEach((key, value) -> assertThat(tableDetails.getPropertyValue(key)).contains(value));
    }

    private void verifyBlueArrowWork(EditorPage editorPage) {
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();

        assertThat(tableDetails.getPropertyRowBackgroundColor("LOB"))
                .isEqualTo("rgba(190, 220, 255, 0.3)");
        assertThat(tableDetails.getPropertyRowTitle("LOB"))
                .isEqualTo("Inherited property");
        assertThat(tableDetails.getGoToPropertiesTableArrowTitle("LOB"))
                .isEqualTo("Go to Properties table");

        tableDetails.clickGoToPropertiesTableArrow("LOB");

        TableComponent centerTable = editorPage.getCenterTable();
        Assert.assertTrue(centerTable.isVisible(), "Properties table should be visible");

        String headerText = String.join(" ", centerTable.getRow(1).getValue());
        assertThat(headerText).contains("Properties myProperty");
    }
}
