package tests.testng;

import com.codeborne.selenide.Selenide;
import org.testng.annotations.Test;
import web.pages.GoogleHomePage;

public class SampleTest extends BaseTest {

    @Test
    public void homePageTest() {
        GoogleHomePage googleHomePage = new GoogleHomePage();
        googleHomePage.open();
        googleHomePage.search("iphone");
        Selenide.sleep(10000);
    }
}
