package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.PlaywrightWebElement;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightArrayDeclarationIsLink extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11230")
    @Description("Verify that array declarations are displayed as links with proper styling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightArrayDeclarationIsLink() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestArrayDeclarationIsLink.xlsx");

        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestArrayDeclarationIsLink");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "DetermineStatusByCodeRule");

        // Find procedure links with title-datatype class
        editorPage.getCenterTable().isVisible();
        List<PlaywrightWebElement> links = editorPage.createElementList("xpath=//td//span[contains(@class,'title-datatype')]/a[text()='Procedure']");
        assertThat(links.size()).as("Should find exactly 12 procedure links").isEqualTo(12);

        // Verify that all procedure links have proper styling
        links.forEach(link -> {
            String borderBottom = link.getCssValue("borderBottom");
            assertThat(borderBottom)
                    .as("Procedure link should have proper border-bottom styling")
                    .satisfiesAnyOf(
                            border -> assertThat(border).contains("1px dotted"),
                            border -> assertThat(border).contains("1px solid")
                    );
        });
    }
}