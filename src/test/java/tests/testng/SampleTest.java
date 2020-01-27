package tests.testng;

import org.testng.annotations.Test;
import web.pages.GoogleHomePage;

public class SampleTest extends BaseSampleTest {

    @Test
    public void homePageTest() {
        GoogleHomePage googleHomePage = new GoogleHomePage();
        googleHomePage.open();
        googleHomePage.search("iphone");
    }
}
