package web.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import domain.PropertyName;
import utils.PropertyHandler;

public abstract class BasePage {

    {
        Configuration.baseUrl = PropertyHandler.getProperty(PropertyName.BASE_URL);
    }

    public void open() {
        Selenide.open("");
    }
}
