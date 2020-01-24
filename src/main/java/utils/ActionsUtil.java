package utils;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.How;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ActionsUtil {

    //To be used for different locator types
    public static SelenideElement findElement(How how, String locator) {
        return $(how.buildBy(locator));
    }

    public static ElementsCollection findElements(How how, String locator) {
        return $$(how.buildBy(locator));
    }
}
