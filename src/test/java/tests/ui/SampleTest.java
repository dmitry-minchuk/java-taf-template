package tests.ui;

import org.testng.annotations.Test;
import tests.BaseTest;
import web.pages.GoogleHomePage;

public class SampleTest extends BaseTest {

    @Test
    public void homePageTest() {
        GoogleHomePage googleHomePage = new GoogleHomePage(getDriver());
        googleHomePage.open();
        googleHomePage.search("iphone");
    }
}
