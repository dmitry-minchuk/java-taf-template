package tests.testng;

import com.codeborne.selenide.Selenide;
import web.pages.amazon.AmazonDealsPage;
import web.pages.amazon.AmazonHomePage;

public class AmazonTest extends BaseTest {
    
    public void test1() {
        AmazonHomePage amazonHomePage = new AmazonHomePage();
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
//        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
        Selenide.sleep(10000);
    }
}
