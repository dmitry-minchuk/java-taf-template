package tests.api;

import api.methods.tms.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TmsApiTest extends BaseTest {
    private String jwt;

    @BeforeMethod
    public void getAuth() {
        jwt = new GetAuthMethod().getJWT();
    }

    @Test
    public void test() {
        new GetAllTrainingsMethod(jwt).callAPI();;
        new PostTrainingMethod(jwt).callAPI();
    }

    @Test
    public void test2() {
        new GetTrainingByIdMethod(jwt).callAPI("3");
    }

    @Test
    public void test3() {
//        new GetAllCoachesMethod(jwt).callAPI();
        String trainingId = "3";
        new GetCommentListMethod(jwt).callAPI(trainingId);
        new PostCommentMethod(jwt).callAPI(trainingId);
        DeleteCommentMethod deleteCommentMethod = new DeleteCommentMethod(jwt);
        new GetCommentListMethod(jwt).callAPI(trainingId);
        deleteCommentMethod.callAPI("263");
        deleteCommentMethod.callAPI("264");
        deleteCommentMethod.callAPI("265");

        new GetCommentListMethod(jwt).callAPI(trainingId);
    }
}
