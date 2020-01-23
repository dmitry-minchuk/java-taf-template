package tests.testng;

import com.codeborne.selenide.Configuration;
import domain.PropertyName;
import utils.PropertyHandler;

public abstract class BaseTest {
    {
        Configuration.remote = PropertyHandler.getProperty(PropertyName.SELENIUM_HOST);
    }
}
