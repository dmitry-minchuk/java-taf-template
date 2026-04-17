package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestVersioningByFolders extends BaseTest {

    private static final String MODULE_NAME = "TestModuleCategoryInheritedProperties";
    private static final String BASE_FOLDER = "Decision";
    private static final String VERSION_FOLDER = "MyRules1";
    private static final String VERSIONED_TABLE_1 = "MyRules1 [0.0.1]";
    private static final String VERSIONED_TABLE_2 = "MyRules1 [0.0.2]";
    private static final String VERSION_VALUE = "v2";
    private static final String PROPERTY_NAME = "LOB";
    private static final String INHERITED_VALUE = "001";
    private static final String OVERRIDDEN_VALUE = "777";

    @Test
    @TestCaseId("IPBQA-30979")
    @Description("Versioning by folders: verify copied versions are grouped under a table folder, inherited properties are preserved per version, and overriding a property in one version does not affect the other version")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testVersioningByFolders() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestModuleCategoryInheritedProperties.xlsx");
        EditorPage editorPage = new EditorPage();
        EditorLeftRulesTreeComponent rulesTree = editorPage.getEditorLeftRulesTreeComponent();
        RightTableDetailsComponent tableDetails = editorPage.getRightTableDetailsComponent();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);

        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(BASE_FOLDER)
                .selectItemInFolder(BASE_FOLDER, VERSION_FOLDER);
        verifyInheritedProperty(tableDetails, INHERITED_VALUE);

        editorPage.getEditorToolbarPanelComponent().copyTableAsNewVersion(VERSION_VALUE);
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(BASE_FOLDER);
        List<String> nodesNames = rulesTree.getAllEndNodesNames();
        assertThat(nodesNames)
                .as("Copied table versions should be grouped together in the visible tree list")
                .containsSequence(VERSIONED_TABLE_1, VERSIONED_TABLE_2);
        assertThat(nodesNames.stream().filter(name -> name.startsWith(VERSION_FOLDER + " [")).count())
                .as("Two versioned entries of the same table should be visible after Copy as New Version")
                .isEqualTo(2);

        rulesTree.selectVisibleLeafNode(VERSIONED_TABLE_2);
        verifyInheritedProperty(tableDetails, INHERITED_VALUE);
        tableDetails.editTextProperty(PROPERTY_NAME, OVERRIDDEN_VALUE);
        tableDetails.clickSaveBtn();

        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(BASE_FOLDER);
        List<String> refreshedNodesNames = rulesTree.getAllEndNodesNames().stream()
                .filter(name -> name.startsWith(VERSION_FOLDER + " ["))
                .toList();

        assertThat(refreshedNodesNames)
                .as("Both versioned table entries should still be present after saving an override")
                .hasSize(2);
        assertThat(String.join(" | ", refreshedNodesNames))
                .as("Overriding a property in one version should not affect the other version")
                .contains("lob=001")
                .contains("lob=777");
    }

    private void verifyInheritedProperty(RightTableDetailsComponent tableDetails, String expectedValue) {
        assertThat(tableDetails.getPropertyValue(PROPERTY_NAME))
                .as("Inherited property value should be visible in table details")
                .contains(expectedValue);
        assertThat(tableDetails.getPropertyRowBackgroundColor(PROPERTY_NAME))
                .as("Inherited property row should stay highlighted")
                .isEqualTo("rgba(190, 220, 255, 0.3)");
        assertThat(tableDetails.getPropertyRowTitle(PROPERTY_NAME))
                .as("Inherited property row should keep the inherited tooltip")
                .isEqualTo("Inherited property");
        assertThat(tableDetails.getGoToPropertiesTableArrowTitle(PROPERTY_NAME))
                .as("Inherited property should keep the blue-arrow navigation to the properties table")
                .isEqualTo("Go to Properties table");
    }
}
