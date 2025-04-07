package domain.ui.webstudio.pages.mainpages;

import configuration.core.SmartElementFactory;
import configuration.driver.DriverPool;

public class EditorPage extends ProxyMainPage {

    public EditorPage() {
        super("/");
        SmartElementFactory.initElements(DriverPool.getDriver(), this);
    }
}
