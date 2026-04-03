package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.ui.webservice.pages.ServicePage;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRuleServicesNewUI extends BaseTest {

    private static final String DEPLOYMENT1 = "deployment1/Sample1";
    private static final String DEPLOYMENT2 = "deployment2/Sample2";
    private static final String DEPLOYMENT3 = "deployment3/Sample3";

    private static final Map<String, String> additionalContainerConfig = new HashMap<>(Map.of(
            "openl.application.title", "🌍 Привет, мир! こんにちは世界 🚀"
    ));

    private static final Map<String, String> additionalContainerFiles = new HashMap<>(Map.of(
            TestDataUtil.getDirectoryPathFromResources("TestRuleServicesNewUI"),
            "/opt/openl/shared/"
    ));

    @Test
    @TestCaseId("EPBDS-14196")
    @Description("Verify new Rule Services UI features: configurable title, Show All Deployments filter, Services & Links column")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_FILE_PARAMS)
    public void testRuleServicesNewUI() {
        ServicePage servicePage = new ServicePage(LocalDriverPool.getPage());
        servicePage.open();

        // Step 1: All 3 deployments are visible (confirms AJAX completed and JS rendered the page)
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT1).isVisible(5000))
                .as(DEPLOYMENT1 + " should be visible")
                .isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT2).isVisible(5000))
                .as(DEPLOYMENT2 + " should be visible")
                .isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT3).isVisible(5000))
                .as(DEPLOYMENT3 + " should be visible")
                .isTrue();

        // Step 2: Verify configurable page title (set by JS after AJAX — check after deployments are rendered)
        assertThat(LocalDriverPool.getPage().title())
                .as("Page title should match the configured openl.application.title")
                .isEqualTo("🌍 Привет, мир! こんにちは世界 🚀");

        // Step 3: "Show all deployments" checkbox is checked by default
        assertThat(servicePage.getShowAllDeploymentsCheckBox().isChecked())
                .as("'Show all deployments' checkbox should be checked by default")
                .isTrue();

        // Step 4: Uncheck "Show all deployments" - deployment1/Sample1 disappears
        servicePage.getShowAllDeploymentsCheckBox().uncheck();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT1).isVisible(2000))
                .as(DEPLOYMENT1 + " should NOT be visible when 'Show all deployments' is unchecked")
                .isFalse();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT2).isVisible(3000))
                .as(DEPLOYMENT2 + " should be visible when 'Show all deployments' is unchecked")
                .isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT3).isVisible(3000))
                .as(DEPLOYMENT3 + " should be visible when 'Show all deployments' is unchecked")
                .isTrue();

        // Step 5: Re-check "Show all deployments" - deployment1/Sample1 reappears
        servicePage.getShowAllDeploymentsCheckBox().check();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT1).isVisible(5000))
                .as(DEPLOYMENT1 + " should be visible after re-checking 'Show all deployments'")
                .isTrue();

        // Step 6: deployment1/Sample1 shows KAFKA badge and MANIFEST.MF in Services & Links
        assertThat(servicePage.getDeploymentKafka(DEPLOYMENT1).isVisible(3000))
                .as(DEPLOYMENT1 + " should have KAFKA badge in Services & Links")
                .isTrue();
        assertThat(servicePage.getDeploymentManifestLink(DEPLOYMENT1).isVisible(3000))
                .as(DEPLOYMENT1 + " should have MANIFEST.MF link in Services & Links")
                .isTrue();

        // Step 7: Click MANIFEST.MF, verify content, navigate back
        servicePage.getDeploymentManifestLink(DEPLOYMENT1).click();
        String pageContent = LocalDriverPool.getPage().content();
        assertThat(pageContent)
                .as("MANIFEST.MF page should contain implementation info for Sample1")
                .contains("Implementation-Title")
                .contains("Sample1")
                .contains("Build-Branch")
                .contains("master");
        LocalDriverPool.getPage().goBack();
        servicePage = new ServicePage(LocalDriverPool.getPage());

        // Wait for all 3 rows to appear after back navigation — ensures AJAX has completed
        // and all Services & Links badges are fully rendered before continuing
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT1).isVisible(5000))
                .as("After back navigation, " + DEPLOYMENT1 + " should be visible")
                .isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT2).isVisible(5000))
                .as("After back navigation, " + DEPLOYMENT2 + " should be visible")
                .isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT3).isVisible(5000))
                .as("After back navigation, " + DEPLOYMENT3 + " should be visible")
                .isTrue();

        // Step 8: deployment2/Sample2 has no KAFKA and no RMI in Services & Links, but has MANIFEST.MF
        // (WS auto-generates MANIFEST info for all deployments regardless of MANIFEST.MF file presence)
        assertThat(servicePage.getDeploymentKafka(DEPLOYMENT2).isVisible(2000))
                .as(DEPLOYMENT2 + " should NOT have KAFKA in Services & Links")
                .isFalse();
        assertThat(servicePage.getDeploymentRmiLink(DEPLOYMENT2).isVisible(2000))
                .as(DEPLOYMENT2 + " should NOT have RMI link (new UI hides RMI from Services & Links)")
                .isFalse();
        assertThat(servicePage.getDeploymentManifestLink(DEPLOYMENT2).isVisible(2000))
                .as(DEPLOYMENT2 + " should NOT have visible MANIFEST.MF link (no MANIFEST.MF file — CSS display:none via .row:not(.hasManifest))")
                .isFalse();

        // Step 9: [V] icon for deployment1/Sample1 is present but clicking it expands nothing
        assertThat(servicePage.getDeploymentSuccessIcon(DEPLOYMENT1).isVisible(3000))
                .as("[V] icon should be present for successfully deployed " + DEPLOYMENT1)
                .isTrue();
        servicePage.getDeploymentSuccessIcon(DEPLOYMENT1).click();
        assertThat(servicePage.getListMethods("sample1").isVisible(1000))
                .as("No methods list should appear after clicking [V] for " + DEPLOYMENT1)
                .isFalse();

        // Step 10: [X] icon for deployment3/Sample3 expands error message
        servicePage.getDeploymentFailedIcon(DEPLOYMENT3).click();
        assertThat(servicePage.getDeploymentErrorText(DEPLOYMENT3).isVisible(3000))
                .as("Error text should appear after clicking [X] for " + DEPLOYMENT3)
                .isTrue();
        assertThat(servicePage.getDeploymentErrorText(DEPLOYMENT3).getText())
                .as("Error message content for " + DEPLOYMENT3)
                .contains("Identifier 'max' is not found.");

        // Step 11: deployment1/Sample1 title has swagger link but it is CSS-disabled
        // (pointer-events:none because it's not a RESTFUL service — class="row service-KAFKA")
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT1).isVisible(3000))
                .as(DEPLOYMENT1 + " swagger title link should be present in DOM")
                .isTrue();
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT1).getCssValue("pointer-events"))
                .as(DEPLOYMENT1 + " swagger link should be CSS-disabled (not clickable for non-RESTFUL)")
                .isEqualTo("none");

        // Step 12: deployment3/Sample3 title has swagger link but it is CSS-disabled
        // (pointer-events:none because it is a failed service — class="row status-failed")
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT3).isVisible(3000))
                .as(DEPLOYMENT3 + " swagger title link should be present in DOM")
                .isTrue();
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT3).getCssValue("pointer-events"))
                .as(DEPLOYMENT3 + " swagger link should be CSS-disabled (not clickable for failed service)")
                .isEqualTo("none");

        // Step 13: deployment2/Sample2 title IS a clickable link to its swagger UI
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT2).isVisible(3000))
                .as(DEPLOYMENT2 + " title should be a clickable link")
                .isTrue();
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT2).getCssValue("pointer-events"))
                .as(DEPLOYMENT2 + " swagger link should be clickable (RESTFUL service)")
                .isEqualTo("auto");
        assertThat(servicePage.getProjectTitleLink(DEPLOYMENT2).getAttribute("href"))
                .as(DEPLOYMENT2 + " swagger link href")
                .contains("swagger-ui.html")
                .contains("sample2");
    }
}
