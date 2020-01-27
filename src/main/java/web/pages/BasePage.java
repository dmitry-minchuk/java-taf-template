package web.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import domain.PropertyNameSpace;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import utils.ProjectConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BasePage {
    protected static final Logger LOGGER = LogManager.getLogger(BasePage.class);
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
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        String env = ProjectConfiguration.getProperty(PropertyNameSpace.ENV);
        LOGGER.info("Environment: " + env);

        if( env.length() == 0) {
            Configuration.baseUrl = ProjectConfiguration.getProperty(PropertyNameSpace.BASE_URL);
        } else {
            List<String> propertyNameList = ProjectConfiguration.getProperties().stream().filter(p -> p.contains(".")
                    && p.substring(0, p.indexOf(".")).equals(env)
                    && p.substring(p.indexOf(".") + 1).equals(PropertyNameSpace.BASE_URL.getValue())).collect(Collectors.toList());

            if(propertyNameList.isEmpty()) {
                throw new RuntimeException("Base url was not found for given env parameter. Env parameter: " + env);
            } else if(propertyNameList.size() > 1) {
                StringBuilder sb = new StringBuilder();
                propertyNameList.forEach(p -> {sb.append(p); sb.append(" ");});
                throw new RuntimeException("Extra Base url was found for given env parameter. Env parameter: " + env + ". Found base urls: " + sb);
            }

            Configuration.baseUrl = ProjectConfiguration.getProperty(propertyNameList.get(0));
        }
    }
}
