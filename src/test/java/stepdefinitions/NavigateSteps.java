package stepdefinitions;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.When;

public class NavigateSteps {

    private final static String HTTP_PREFIX = "http://";
    private final String navigateToUrl = "^User navigates to \"([^\"]*)\"$";
    private final String navigateToUrlWithPath = "^User navigates to \"([^\"]*)\" with .* \"([^\"]*)\"$";

    @When(value = navigateToUrl)
    public void navigateToUrl(String url) {
        Selenide.open(HTTP_PREFIX + url);
    }

    @When(value = navigateToUrlWithPath)
    public void navigateToUrlWithPath(String url, String path) {
        Selenide.open(HTTP_PREFIX + url + path);
    }
}
