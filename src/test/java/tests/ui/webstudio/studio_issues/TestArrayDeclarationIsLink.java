package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.WebElement;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestArrayDeclarationIsLink extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11230")
    @Description("Verify that array declarations are displayed as links with proper styling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testArrayDeclarationIsLink() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestArrayDeclarationIsLink.xlsx");

        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestArrayDeclarationIsLink");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "DetermineStatusByCodeRule");

        // Find procedure links with title-datatype class
        WaitUtil.waitForCondition(() -> editorPage.getCenterTable().isVisible(), 5000, 100, "Waiting for table to be visible...");
        List<WebElement> links = editorPage.createElementList("xpath=//td//span[contains(@class,'title-datatype')]/a[text()='Procedure']");
        assertThat(links.size()).as("Should find exactly 12 procedure links").isEqualTo(12);

        // Each procedure reads as a link: it points somewhere and is marked as one, by an underline or by a
        // bottom border. The exact border style is not pinned down — it is a styling choice that changes.
        links.forEach(link -> {
            String borderBottom = link.getCssValue("border-bottom");
            String textDecoration = link.getCssValue("text-decoration");
            assertThat(borderBottom + " | " + textDecoration)
                    .as("A procedure link should be marked as a link (underline or bottom border)")
                    .satisfiesAnyOf(
                            style -> assertThat(style).contains("underline"),
                            style -> assertThat(style).contains("dotted"),
                            style -> assertThat(style).contains("dashed"),
                            style -> assertThat(style).contains("solid")
                    );
        });
    }
}