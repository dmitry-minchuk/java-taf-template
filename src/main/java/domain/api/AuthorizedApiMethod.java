package domain.api;

import com.microsoft.playwright.options.Cookie;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import helpers.service.UserService;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public abstract class AuthorizedApiMethod extends ApiBaseMethod {

    private static final ThreadLocal<SessionFilter> SESSION_FILTER = new ThreadLocal<>();
    // OIDC/oauth2 mode: REST setup authenticates with a bearer token instead of basic auth.
    private static final ThreadLocal<String> BEARER_TOKEN = new ThreadLocal<>();

    protected AuthorizedApiMethod(String path) {
        super(path);
    }

    public static void startSession() {
        SESSION_FILTER.set(new SessionFilter());
    }

    public static void clearSession() {
        SESSION_FILTER.remove();
    }

    public static void setBearerToken(String token) {
        BEARER_TOKEN.set(token);
    }

    public static void clearBearerToken() {
        BEARER_TOKEN.remove();
    }

    protected RequestSpecification authorizedRequest() {
        RequestSpecification specification = RestAssured.given().header("Accept", "application/json");

        String bearer = BEARER_TOKEN.get();
        if (bearer != null) {
            return specification.header("Authorization", "Bearer " + bearer);
        }

        SessionFilter sessionFilter = SESSION_FILTER.get();
        if (sessionFilter != null) {
            specification.filter(sessionFilter);
        }

        List<Cookie> cookies = extractBrowserSessionCookies();
        if (!cookies.isEmpty()) {
            cookies.forEach(cookie -> specification.cookie(cookie.name, cookie.value));
            return specification;
        }

        UserData admin = UserService.getUser(User.ADMIN);
        return specification.auth()
                .preemptive()
                .basic(admin.getLogin(), admin.getPassword());
    }

    protected RequestSpecification authorizedJsonRequest(Object body) {
        return authorizedRequest()
                .contentType(ContentType.JSON)
                .body(body);
    }

    private List<Cookie> extractBrowserSessionCookies() {
        try {
            if (LocalDriverPool.getPage() == null) {
                return List.of();
            }
            return LocalDriverPool.getPage().context().cookies();
        } catch (Exception e) {
            LOGGER.debug("Unable to read Playwright session cookies, falling back to basic auth: {}", e.getMessage());
            return List.of();
        }
    }
}
