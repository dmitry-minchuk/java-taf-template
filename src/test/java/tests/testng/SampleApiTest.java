package tests.testng;

import api.methods.GetSampleResource;
import org.testng.annotations.Test;

public class SampleApiTest extends BaseSampleTest {

    @Test
    public void testSampleApi() {
        GetSampleResource getSampleResource = new GetSampleResource();
        getSampleResource.get();
    }
}
