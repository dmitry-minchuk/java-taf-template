package web.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import domain.PropertyNameSpace;
import utils.ProjectConfiguration;

public abstract class BasePage {
    protected String urlAppender = "";

    protected BasePage() {
        initBaseUrl();
    }

    protected BasePage(String urlAppender) {
        initBaseUrl();
        this.urlAppender = urlAppender;
    }

    public void open() {
        Selenide.open(urlAppender);
    }

    private void initBaseUrl() {
        Configuration.baseUrl = ProjectConfiguration.getProperty(PropertyNameSpace.BASE_URL);
    }
}
