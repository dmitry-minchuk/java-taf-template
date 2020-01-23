package tests.testng;

import com.codeborne.selenide.Selenide;
import org.testng.annotations.Test;
import web.pages.OnlinerHomePage;

public class SampleTest extends BaseTest {

    @Test
    public void homePageTest() {
        OnlinerHomePage onlinerHomePage = new OnlinerHomePage();
        onlinerHomePage.open();
        Selenide.sleep(10000);
    }
}
