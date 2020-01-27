package stepdefinitions;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.When;
import org.openqa.selenium.Keys;

public class KeypadSteps {
    private final String pressEscape = "User presses Escape Btn";

    @When(value = pressEscape)
    public void pressEscape() {
        Selenide.sleep(1000);
        Selenide.actions().sendKeys(Keys.ESCAPE).build().perform();
    }
}
