package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import web.pages.AmazonDealsPage;
import web.pages.AmazonHomePage;

public class AmazonTest extends BaseTest {

    @Test
    public void test1() {
        AmazonHomePage amazonHomePage = new AmazonHomePage();
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
    }
}
