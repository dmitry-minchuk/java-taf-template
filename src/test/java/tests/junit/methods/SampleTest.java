package tests.junit.methods;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tests.testng.BaseTest;
import web.pages.google.GoogleHomePage;
import web.pages.openl.WebstudioHomePage;

@Testcontainers
public class SampleTest extends BaseTest {

    @Container
    private GenericContainer browser = new FixedHostPortGenericContainer("selenium/standalone-chrome-debug")
            .withFixedExposedPort(4444, 4444)
            .withFixedExposedPort(5900, 5900)
            .waitingFor(Wait.forHttp("/"));

//    @Test
    public void homePageTest() {
        GoogleHomePage googleHomePage = new GoogleHomePage();
        googleHomePage.open();
        googleHomePage.search("iphone");
    }

    @Test
    public void homePageTest2() {
        WebstudioHomePage webstudioHomePage = new WebstudioHomePage();
        webstudioHomePage.open();
        webstudioHomePage.login("admin", "admin");
    }

    @Test
    public void homePageTest3() {
        WebstudioHomePage webstudioHomePage = new WebstudioHomePage();
        webstudioHomePage.open();
        webstudioHomePage.login("admin", "admin");
    }
}
