package api.methods.movies;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

public class GetMovieByMethod extends ApiBaseMethod {
    private final RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addHeader("x-rapidapi-host", "movie-database-imdb-alternative.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "75c2cb2485msh01881e6d1ca2379p1d4ee3jsn4cea17875d52")
            .build();

    public GetMovieByMethod() {
        super("");
    }

    public Response getMovieByName(String movieName) {
        RequestSpecification r = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecification)
                .addParam("s", movieName)
                .build();
        return callApi(Method.GET, r);
    }

    public Response getMovieById(String movieId) {
        RequestSpecification r = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecification)
                .addParam("i", movieId)
                .build();
        return callApi(Method.GET, r);
    }

    public String getImdbId(String json, int index) {
        JSONObject searchItem = (JSONObject) new JSONObject(json).getJSONArray("Search").get(index);
        return searchItem.getString("imdbID");
    }

    public String getMovieActors(String json) {
        return new JSONObject(json).getString("Actors");
    }
}
