package tests;

import org.testng.annotations.Test;
import web.pages.GoogleHomePage;

public class SampleTest extends BaseTest {

    @Test
    public void homePageTest() {
        GoogleHomePage googleHomePage = new GoogleHomePage();
        googleHomePage.open();
        googleHomePage.search("iphone");
    }
}
