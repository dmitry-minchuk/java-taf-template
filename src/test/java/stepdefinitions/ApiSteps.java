package stepdefinitions;

import api.methods.GetSampleResourceMethod;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ApiSteps {

    private final String requestApiEndpoint = "^User requests \"([^\"]*)\" endpoint$";
    private final String statusCOde200 = "^Status code is 200$";

    @When(value = requestApiEndpoint)
    public void requestApiEndpoint(String path) {
        GetSampleResourceMethod getSampleResourceMethod = new GetSampleResourceMethod(path);
        getSampleResourceMethod.callApi();
    }

    @Then(value = statusCOde200)
    public void statusCOde200() {
        // some status code validation here
    }
}
