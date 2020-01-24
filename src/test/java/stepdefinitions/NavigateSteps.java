package stepdefinitions;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.When;

public class NavigateSteps {

    private final String navigateTo = "^User navigates to \"([^\"]*)\"$";

    @When(value = navigateTo)
    public void navigateTo(String url) {
        Selenide.open("https://" + url);
    }
}
