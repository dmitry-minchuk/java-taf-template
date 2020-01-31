package tests.testng;

import api.methods.GetSampleResource;
import api.methods.PostSampleResource;
import org.testng.annotations.Test;

public class SampleApiTest extends BaseSampleTest {

    @Test
    public void testSampleGetApi() {
        GetSampleResource getSampleResource = new GetSampleResource();
        getSampleResource.callApi();
    }

    @Test
    public void testSamplePostApi() {
        PostSampleResource postSampleResource = new PostSampleResource();
        postSampleResource.callApi();
    }
}
