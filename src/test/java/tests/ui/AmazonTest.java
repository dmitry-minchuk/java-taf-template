package tests.ui;

import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;
import domain.ui.pages.AmazonDealsPage;
import domain.ui.pages.AmazonHomePage;

public class AmazonTest extends BaseTest {

    @Test
    public void test1() {
        AmazonHomePage amazonHomePage = new AmazonHomePage(getDriver());
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
    }

    @Test
    public void test2() {
        AmazonHomePage amazonHomePage = new AmazonHomePage(getDriver());
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
    }

    @Test
    public void test3() {
        AmazonHomePage amazonHomePage = new AmazonHomePage(getDriver());
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
    }

    @Test
    public void test4() {
        AmazonHomePage amazonHomePage = new AmazonHomePage(getDriver());
        amazonHomePage.open();
        AmazonDealsPage amazonDealsPage = amazonHomePage.clickDealsLink();
        Assert.assertTrue(amazonDealsPage.isPageOpened(), "Amazon Deals Page was not opened!");
        amazonDealsPage.header.selectSearchArea("Electronics", "cell phones");
    }
}
