package tests.ui.webstudio;

import configuration.appcontainer.AppContainerFactory;
import configuration.driver.DriverFactory;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;

public class SampleTest3 {

    @Test
    public void test() {
        AppContainerFactory.createContainer(StringUtil.generateUniqueName());
        DriverFactory.getContainerizedDriver();
        System.out.println();
    }

    @Test
    public void test2() {
        AppContainerFactory.createContainer(StringUtil.generateUniqueName());
        DriverFactory.getContainerizedDriver();
        System.out.println();
    }
}
