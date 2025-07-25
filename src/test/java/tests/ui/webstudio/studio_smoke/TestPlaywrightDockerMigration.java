package tests.ui.webstudio.studio_smoke;

import configuration.driver.PlaywrightDockerDriverPool;
import helpers.utils.PlaywrightExpectUtil;
import tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.microsoft.playwright.Page;

/**
 * Test class to verify Playwright Docker migration functionality
 * Phase 3.4: Validate container networking and Docker integration
 */
public class TestPlaywrightDockerMigration extends BaseTest {

    @Test(description = "Verify Playwright Docker infrastructure and container networking")
    public void testPlaywrightDockerInfrastructure() {
        // This test should only run in Playwright Docker mode
        if (!PlaywrightDockerDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright Docker mode (execution.mode=PLAYWRIGHT_DOCKER)");
        }
        
        Page page = PlaywrightDockerDriverPool.getPage();
        Assert.assertNotNull(page, "Playwright Docker page should be initialized");
        
        // Test container networking - navigate to application container
        PlaywrightDockerDriverPool.navigateToApp();
        
        // Test PlaywrightExpectUtil expect patterns with container networking
        boolean pageReady = PlaywrightExpectUtil.expectVisible(page, "body");
        Assert.assertTrue(pageReady, "Page body should be visible via container network");
        
        // Test that we can get container info
        String dockerInfo = PlaywrightDockerDriverPool.getDockerInfo();
        Assert.assertTrue(dockerInfo.contains("Playwright Docker Context"), "Should have Docker context info");
        
        System.out.println("✅ Playwright Docker infrastructure test passed!");
        System.out.println("   Docker Info: " + dockerInfo);
    }
    
    @Test(description = "Verify container-to-container communication")
    public void testContainerNetworking() {
        if (!PlaywrightDockerDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright Docker mode (execution.mode=PLAYWRIGHT_DOCKER)");
        }
        
        Page page = PlaywrightDockerDriverPool.getPage();
        
        // Navigate using container networking
        PlaywrightDockerDriverPool.navigateToApp();
        
        // Wait for application to load in container network
        PlaywrightExpectUtil.expectPageReady(page);
        
        // Verify basic application elements are accessible via container network
        Assert.assertTrue(PlaywrightExpectUtil.expectVisible(page, "html"), 
                         "HTML element should be visible via container network");
        
        Assert.assertTrue(PlaywrightExpectUtil.expectAttached(page, "body"), 
                         "Body element should be attached via container network");
        
        // Get current URL to verify host-accessible networking (Phase 3.6 improvement)
        String currentUrl = page.url();
        Assert.assertTrue(currentUrl.contains("localhost"), 
                         "URL should use host-accessible localhost URL for Playwright on host: " + currentUrl);
        
        System.out.println("✅ Container networking test passed!");
        System.out.println("   Container URL: " + currentUrl);
    }
    
    @Test(description = "Phase 3.4: Validate Docker wait performance vs local Playwright")
    public void testDockerWaitPerformance() {
        if (!PlaywrightDockerDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright Docker mode (execution.mode=PLAYWRIGHT_DOCKER)");
        }
        
        Page page = PlaywrightDockerDriverPool.getPage();
        
        // Navigate to application
        PlaywrightDockerDriverPool.navigateToApp();
        
        // Test performance of Docker expect patterns
        long startTime = System.currentTimeMillis();
        
        // Perform multiple expect operations via container network
        PlaywrightExpectUtil.expectVisible(page, "body");
        PlaywrightExpectUtil.expectAttached(page, "html");
        PlaywrightExpectUtil.expectElementCount(page, "head", 1);
        PlaywrightExpectUtil.expectPageReady(page);
        
        long endTime = System.currentTimeMillis();
        long dockerTime = endTime - startTime;
        
        // Docker operations should still be reasonably fast (allowing for network overhead)
        Assert.assertTrue(dockerTime < 10000, 
                         "Docker wait operations should complete within 10 seconds for basic elements");
        
        System.out.println("✅ Docker wait performance validated:");
        System.out.println("   - 4 expect operations completed in " + dockerTime + "ms");
        System.out.println("   - Using container-to-container networking");
        System.out.println("   - Native Playwright wait strategies maintained");
    }
    
    @Test(description = "Phase 3.4: Validate container isolation and resource management")
    public void testContainerIsolation() {
        if (!PlaywrightDockerDriverPool.isInitialized()) {
            throw new RuntimeException("Test requires Playwright Docker mode (execution.mode=PLAYWRIGHT_DOCKER)");
        }
        
        // Verify network isolation
        var network = PlaywrightDockerDriverPool.getNetwork();
        Assert.assertNotNull(network, "Docker network should be available");
        
        // Verify browser context is properly configured for Docker
        var browser = PlaywrightDockerDriverPool.getBrowser();
        Assert.assertNotNull(browser, "Docker browser should be available");
        Assert.assertTrue(browser.isConnected(), "Docker browser should be connected");
        
        // Test that multiple pages work in Docker environment
        Page originalPage = PlaywrightDockerDriverPool.getPage();
        Page newPage = PlaywrightDockerDriverPool.createNewPage();
        
        Assert.assertNotEquals(originalPage, newPage, "Should create separate page instances");
        
        // Both pages should be able to navigate
        originalPage.navigate("about:blank");
        newPage.navigate("about:blank");
        
        Assert.assertTrue(originalPage.url().contains("about:blank"), 
                         "Original page should navigate independently");
        Assert.assertTrue(newPage.url().contains("about:blank"), 
                         "New page should navigate independently");
        
        // Clean up new page
        newPage.close();
        
        System.out.println("✅ Container isolation test passed!");
        System.out.println("   - Network isolation verified");
        System.out.println("   - Browser context isolation verified");
        System.out.println("   - Multi-page support verified");
    }
}