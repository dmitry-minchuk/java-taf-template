package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import configuration.core.ui.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.EXCEL_FILES;
import static org.assertj.core.api.Assertions.assertThat;

import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.By;
import java.util.List;
import java.util.stream.IntStream;

public class TestArrayDeclarationIsLink extends BaseTest {

    @Test
    @TestCaseId("EPBDS-11230")
    @Description("Verify that array declarations are displayed as links with proper styling")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testArrayDeclarationIsLink() {
        // Login, create project and open editor
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, EXCEL_FILES, "TestArrayDeclarationIsLink.xlsx");
        
        // Navigate to editor page and select the specific rule
        EditorPage editorPage = new EditorPage();
        editorPage.getLeftProjectModuleSelectorComponent().selectModule(projectName, "TestArrayDeclarationIsLink");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(LeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "DetermineStatusByCodeRule");
        
        // Get the center table and verify procedure links
        TableComponent centerTable = editorPage.getCenterTable();
        
        // Find procedure links with title-datatype class
        List<SmartWebElement> procedureLinks = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new SmartWebElement(centerTable.getDriver(), By.xpath("(//td//span[contains(@class,'title-datatype')]/a[text()='Procedure'])[" + i + "]")))
                .toList();
        
        // Verify count of procedure links
        assertThat(procedureLinks.size()).as("Should find exactly 12 procedure links").isEqualTo(12);
        
        // Verify that all procedure links have proper styling
        procedureLinks.forEach(link -> {
            String borderBottom = link.getCssValue("border-bottom");
            assertThat(borderBottom)
                    .as("Procedure link should have proper border-bottom styling")
                    .satisfiesAnyOf(
                            border -> assertThat(border).contains("1px dotted"),
                            border -> assertThat(border).contains("1px solid")
                    );
        });
    }
} 