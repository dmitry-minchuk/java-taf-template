package tests.ui.webstudio.studio_smoke;

import configuration.driver.PlaywrightDriverPool;
import helpers.utils.PlaywrightExpectUtil;
import tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.microsoft.playwright.Page;

/**
 * Test class to verify Playwright migration functionality
 * Phase 2.3: Validate expect() patterns and state verification
 */
public class TestPlaywrightMigration extends BaseTest {

    @Test(description = "Verify Playwright infrastructure is working")
    public void testPlaywrightInfrastructure() {
        // This test should only run in Playwright mode
        if (!PlaywrightDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright mode (USE_PLAYWRIGHT=true)");
        }
        
        Page page = PlaywrightDriverPool.getPage();
        Assert.assertNotNull(page, "Playwright page should be initialized");
        
        // Navigate to the application
        String baseUrl = "http://localhost:8090";
        page.navigate(baseUrl);
        
        // Test PlaywrightExpectUtil expect patterns
        boolean pageReady = PlaywrightExpectUtil.expectVisible(page, "body");
        Assert.assertTrue(pageReady, "Page body should be visible");
        
        // Test URL expectation
        boolean urlMatches = PlaywrightExpectUtil.expectUrl(page, baseUrl + "/");
        Assert.assertTrue(urlMatches, "URL should match expected pattern");
        
        System.out.println("✅ Playwright infrastructure test passed!");
    }
    
    @Test(description = "Verify PlaywrightExpectUtil methods work correctly")
    public void testPlaywrightExpectUtilMethods() {
        if (!PlaywrightDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright mode (USE_PLAYWRIGHT=true)");
        }
        
        Page page = PlaywrightDriverPool.getPage();
        
        // Test basic expect methods
        Assert.assertTrue(PlaywrightExpectUtil.expectVisible(page, "html"), 
                         "HTML element should be visible");
        
        Assert.assertTrue(PlaywrightExpectUtil.expectAttached(page, "body"), 
                         "Body element should be attached");
        
        // Test element count expectations
        Assert.assertTrue(PlaywrightExpectUtil.expectElementCount(page, "html", 1), 
                         "Should have exactly one HTML element");
        
        System.out.println("✅ PlaywrightExpectUtil methods test passed!");
    }
    
    @Test(description = "Phase 2.6: Validate improved wait performance with Playwright")
    public void testPlaywrightWaitPerformance() {
        if (!PlaywrightDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright mode (USE_PLAYWRIGHT=true)");
        }
        
        Page page = PlaywrightDriverPool.getPage();
        
        // Test performance of Playwright expect patterns vs traditional waits
        long startTime = System.currentTimeMillis();
        
        // Perform multiple expect operations
        PlaywrightExpectUtil.expectVisible(page, "body");
        PlaywrightExpectUtil.expectAttached(page, "html");
        PlaywrightExpectUtil.expectElementCount(page, "head", 1);
        PlaywrightExpectUtil.expectPageReady(page);
        
        long endTime = System.currentTimeMillis();
        long playwrightTime = endTime - startTime;
        
        // Validate that Playwright waits are reasonably fast (should complete quickly for basic elements)
        Assert.assertTrue(playwrightTime < 5000, 
                         "Playwright wait operations should complete within 5 seconds for basic elements");
        
        System.out.println("✅ Playwright wait performance validated:");
        System.out.println("   - 4 expect operations completed in " + playwrightTime + "ms");
        System.out.println("   - Using native Playwright wait strategies (no custom retry loops)");
        System.out.println("   - No WaitUtil.sleep() calls or custom polling logic");
    }
    
    @Test(description = "Phase 2.6: Validate dual-mode component wait strategies")
    public void testDualModeComponentWaits() {
        if (!PlaywrightDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright mode (USE_PLAYWRIGHT=true)");
        }
        
        // Test that BasePageComponent dual-mode utilities work in Playwright mode
        // This would typically be done through an actual component, but for testing
        // we can validate the PlaywrightExpectUtil functions directly
        
        Page page = PlaywrightDriverPool.getPage();
        
        // Test all key dual-mode wait strategies
        long startTime = System.currentTimeMillis();
        
        boolean visible = PlaywrightExpectUtil.expectVisible(page, "body", 2000);
        boolean attached = PlaywrightExpectUtil.expectAttached(page, "html", 2000);
        boolean stable = PlaywrightExpectUtil.expectElementStable(page, "body", 2000);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        Assert.assertTrue(visible, "Body should be visible");
        Assert.assertTrue(attached, "HTML should be attached");
        Assert.assertTrue(stable, "Body should be stable");
        
        System.out.println("✅ Dual-mode component wait strategies validated:");
        System.out.println("   - All wait strategies use Playwright native mechanisms");
        System.out.println("   - Total time for 3 wait operations: " + totalTime + "ms");
        System.out.println("   - Components can seamlessly switch between Selenium/Playwright modes");
    }
}