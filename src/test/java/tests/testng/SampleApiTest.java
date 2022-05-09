package tests.testng;

import api.methods.httpbin.GetImageMethod;
import api.methods.httpbin.GetSampleResourceMethod;
import api.methods.httpbin.PostSampleResourceMethod;
import api.methods.httpbin.PutAnythingMethod;
import api.methods.movies.GetMovieByMethod;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SampleApiTest extends BaseTest {

    // HTTPBIN tests
//    @Test
    public void testSampleGetApi() {
        GetSampleResourceMethod getSampleResourceMethod = new GetSampleResourceMethod();
        getSampleResourceMethod.getAll();
    }

//    @Test
    public void testSamplePostApi() {
        PostSampleResourceMethod postSampleResourceMethod = new PostSampleResourceMethod();
        postSampleResourceMethod.changeRole();
        postSampleResourceMethod.validateResponseAgainstSchema();
    }

//    @Test
    public void testPutAnythingApi() {
        PutAnythingMethod putAnythingMethod = new PutAnythingMethod("010100111000100101110101010");
        putAnythingMethod.putValue();
    }

//    @Test
    public void testGetImageApi() {
        GetImageMethod getImageMethod = new GetImageMethod();
        Response response = getImageMethod.getImage();
        File image = new File("src/main/resources/image.png");
        try {
            OutputStream outputStream = new FileOutputStream(image);
            IOUtils.copy(response.getBody().asInputStream(), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Movie Database tests
//    @Test
    public void testGetMovieByName() {
        String actorName = "Robert Downey Jr.";

        GetMovieByMethod getMovieByMethod = new GetMovieByMethod();
        Response response = getMovieByMethod.getMovieByName("Avengers");
        String movieId = getMovieByMethod.getImdbId(response.getBody().asString(), 0);
        response = getMovieByMethod.getMovieById(movieId);
//        Assert.assertTrue(getMovieByMethod.getMovieActors(response.getBody().asString()).contains(actorName), "Actor Name is not as expected!");
    }
}
