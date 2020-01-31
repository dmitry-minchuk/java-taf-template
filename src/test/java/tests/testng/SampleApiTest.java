package tests.testng;

import api.methods.GetSampleResourceMethod;
import api.methods.PostSampleResourceMethod;
import org.testng.annotations.Test;

public class SampleApiTest extends BaseSampleTest {

    @Test
    public void testSampleGetApi() {
        GetSampleResourceMethod getSampleResourceMethod = new GetSampleResourceMethod();
        getSampleResourceMethod.callApi();
    }

    @Test
    public void testSamplePostApi() {
        PostSampleResourceMethod postSampleResourceMethod = new PostSampleResourceMethod();
        postSampleResourceMethod.callApi();
        postSampleResourceMethod.validateResponseAgainstSchema();
    }
}
