package tests.testng;

import com.codeborne.selenide.Selenide;
import org.testng.Assert;
import org.testng.annotations.Test;
import web.pages.AmazonDealsPage;
import web.pages.AmazonHomePage;

public class AmazonTest extends BaseSampleTest{

    @Test
    public void test1() {
        AmazonHomePage amazonHomePage = new AmazonHomePage();
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
        Selenide.sleep(10000);
    }
}
