package tests.api;

import api.methods.httpbin.GetImageMethod;
import api.methods.httpbin.GetSampleResourceMethod;
import api.methods.httpbin.PostSampleResourceMethod;
import api.methods.httpbin.PutAnythingMethod;
import api.methods.movies.GetMovieByMethod;
import com.epam.reportportal.service.ReportPortal;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.*;
import java.util.Date;

public class SampleApiTest extends BaseTest {

    // HTTPBIN tests
    @Test
    public void testSampleGetApi() {
        GetSampleResourceMethod getSampleResourceMethod = new GetSampleResourceMethod();
        getSampleResourceMethod.getAll();
    }

    @Test
    public void testSamplePostApi() {
        PostSampleResourceMethod postSampleResourceMethod = new PostSampleResourceMethod();
        postSampleResourceMethod.changeRole();
        postSampleResourceMethod.validateResponseAgainstSchema();
    }

    @Test
    public void testPutAnythingApi() {
        PutAnythingMethod putAnythingMethod = new PutAnythingMethod("010100111000100101110101010");
        putAnythingMethod.putValue();
    }

    @Test
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
        ReportPortal.emitLog("Body: ", "INFO", new Date(), image);
    }

    // Movie Database tests
//    @Test
    public void testGetMovieByName() {
        String actorName = "Robert Downey Jr.";

        GetMovieByMethod getMovieByMethod = new GetMovieByMethod();
        Response response = getMovieByMethod.getMovieByName("Avengers");
        String movieId = getMovieByMethod.getImdbId(response.getBody().asString(), 0);
        response = getMovieByMethod.getMovieById(movieId);
        Assert.assertTrue(getMovieByMethod.getMovieActors(response.getBody().asString()).contains(actorName), "Actor Name is not as expected!");
    }
}
